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
import play.api.libs.openid.Errors.AUTH_CANCEL

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class WiredServiceImpl(system: ActorSystem, persistentService: PersistentService)
                        extends WiredService {
  implicit def executionContext: ExecutionContext = system.dispatcher

  implicit val timeout: Timeout = Timeout(10 seconds)
  val modelManager = system.actorOf(ModelManager.props(persistentService))

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

class ModelManager(persistentService: PersistentService) extends Actor {
  var modelMap: Map[String, ActorRef] = Map.empty

  override def receive: Receive = {
    case GetModel(id) =>
      val ref = modelMap.getOrElse(id, newModel(id))
      sender ! ModelRef(ref)
  }

  def newModel(id: String): ActorRef =
    context.actorOf(WiredModelActor.props(id))
}

object ModelManager {
  def props(persistentService: PersistentService) = Props(classOf[ModelManager], persistentService)

  case class GetModel(id: String)

  case class ModelRef(ref: ActorRef)
}

case class WiredModelActor(id: String, persistentService: PersistentService) extends ModelActor

object WiredModelActor {
  def props(id: String) = Props(classOf[WiredModelActor], id)
}