package com.optrak.testakka.modelutils

import ModelActor.{GreetingResponse, Hello, OnlyMessagesBeginningWithHExcpetion}
import akka.Done
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
      val senderz = sender
      println(s"got greeting message $gm")
      if (gm.message.startsWith("H"))
        for {
          using <- persistentService.useGreeting(id).invoke(gm)
          returned <- persistentService.getGreeting(id).invoke
        } yield {
          assert(returned == gm, "not stored")
          cached = Some(returned)
          using
          senderz ! Done
        }
      else
        throw new OnlyMessagesBeginningWithHExcpetion
    case Hello =>
      println(s"got a hello")
      val senderz = sender
      println(s"sender was $senderz")
      for {
        cachedMessage <- cached.map {
          Future.successful(_)
        }
          .getOrElse(
            persistentService.getGreeting(id).invoke.map { greeting =>
              println(s"got a greeting from service $persistentService")
              cached = Some(greeting)
              greeting
            })
      } yield {
        println(s"gt cached message $cachedMessage")
        senderz ! GreetingResponse(s"${cachedMessage.message}, $id!") }
      }
}

