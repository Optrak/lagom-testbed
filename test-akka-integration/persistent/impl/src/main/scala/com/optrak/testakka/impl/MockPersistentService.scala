package com.optrak.testakka.impl

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.optrak.testakka.api.{GreetingMessage, PersistentService}

import scala.concurrent.Future

class MockPersistentService extends PersistentService {
  val default = "Hello"

  var custom : Map[String, String] = Map.empty

  override def hello(name: String): ServiceCall[NotUsed, String] = ServiceCall { _ =>
    val greetings = custom.getOrElse(name, default)
    Future.successful(s"$greetings, $name!")
  }


  override def useGreeting(id: String): ServiceCall[GreetingMessage, Done] = ServiceCall { gr =>
    custom = custom + (id -> gr.message)
    Future.successful(Done)
  }

  override def getGreeting(id: String): ServiceCall[NotUsed, GreetingMessage] = ServiceCall { gr =>
    println(s"get greeting in mock persistence")
    Future.successful(GreetingMessage(custom.getOrElse(id, default).map(s => (s))))
  }
}
