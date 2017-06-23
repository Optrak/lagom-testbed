package com.optrak.testakka.modelutils

import ModelActor.{GreetingResponse, Hello, OnlyMessagesBeginningWithHExcpetion}
import akka.actor.Actor
import com.optrak.testakka.api.{GreetingMessage, PersistentService}

import scala.concurrent.Future

object ModelActor {

  class OnlyMessagesBeginningWithHExcpetion extends Exception("you can only have greetings beginning with H")

  case object Hello

  case class GreetingResponse(message: String)

}

abstract class ModelActor extends Actor {

  def persistentService: PersistentService

  implicit def executionContext = context.dispatcher

  def id: String

  var cached: Option[GreetingMessage] = None

  // todo - some dodgy use of futures going on here perhaps. Need to check
  override def receive: Receive = {
    case gm: GreetingMessage =>
      if (gm.message.startsWith("H"))
        for {
          using <- persistentService.useGreeting(id).invoke(gm)
          returned <- persistentService.getGreeting(id).invoke
        } yield {
          assert(returned == gm, "not stored")
          cached = Some(returned)
          using
        }
      else
        throw new OnlyMessagesBeginningWithHExcpetion
    case Hello =>
      val senderz = sender()
      val cachedMessage: Future[GreetingMessage] = cached.map { Future.successful(_) }
          .getOrElse(
            persistentService.getGreeting(id).invoke.map { greeting =>
              cached = Some(greeting)
              greeting
            })
      cachedMessage.foreach { m => senderz ! GreetingResponse(s"${m.message}, $id") }
    }
}

