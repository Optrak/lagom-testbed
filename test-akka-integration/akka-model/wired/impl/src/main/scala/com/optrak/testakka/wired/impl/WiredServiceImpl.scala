package com.optrak.testakka.wired.impl

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.optrak.testakka.api.{GreetingMessage, PersistentService}
import com.optrak.testakka.wired.api.WiredService
import com.optrak.testakka.wired.impl.ModelManager.{GetModel, ModelRef}
import akka.pattern._
import akka.util.Timeout
import com.optrak.testakka.modelutils.ModelActor
import com.optrak.testakka.modelutils.ModelActor.{GreetingResponse, Hello}
import com.optrak.testakka.wired.impl.model.{ModelManager, ModelManagerRef}
import com.softwaremill.macwire._
import com.softwaremill.tagging._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
class WiredServiceImpl(modelManager: ActorRef @@ ModelManagerRef)
                      (implicit executionContext: ExecutionContext)
                        extends WiredService {

  implicit val timeout: Timeout = Timeout(10 seconds)
  def getActorModel(id: String): Future[ActorRef] = (modelManager ? GetModel(id)).mapTo[ModelRef].map(_.ref)

  override def hello(id: String): ServiceCall[NotUsed, String] = ServiceCall { _ =>
    for {
      actorModel <- getActorModel(id)
      helloed <- (actorModel ? Hello).mapTo[GreetingResponse]
    } yield helloed.message
  }

  override def useGreeting(id: String): ServiceCall[GreetingMessage, Done] = ServiceCall { gm =>
    for {
      actorModel <- getActorModel(id)
      toldToUse <- (actorModel ? gm).mapTo[Done]
    } yield toldToUse
  }
}

package model {

  case class WiredModelActor(id: String, persistentService: PersistentService) extends ModelActor

  trait ModelManagerRef

  class ModelManager(modelFactory: ModelFactory) extends Actor {
    var modelMap: Map[String, ActorRef] = Map.empty

    override def receive: Receive = {
      case GetModel(id) =>
        val ref = modelMap.getOrElse(id, modelFactory.createModel(id))
        sender ! ModelRef(ref)
    }
  }

  case class ModelFactory(persistentService: PersistentService,
                          actorSystem: ActorSystem) {
    def createModel(id: String): ActorRef = actorSystem.actorOf(Props(wire[WiredModelActor]))
  }

  class ModelManagerFactory(modelFactory: ModelFactory, actorSystem: ActorSystem) {
    def createModelManager: ActorRef @@ ModelManagerRef =
      actorSystem.actorOf(Props(wire[ModelManager])).taggedWith[ModelManagerRef]
  }

  trait ModelModule {
    def actorSystem: ActorSystem

    def persistentService : PersistentService
    lazy val modelFactory = wire[ModelFactory]
    lazy val modelManagerFactory = wire[ModelManagerFactory]
  }
}



object ModelManager {

  case class GetModel(id: String)

  case class ModelRef(ref: ActorRef)

}


