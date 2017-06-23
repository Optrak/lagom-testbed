package com.optrak.testakka.impl

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import com.optrak.testakka.api.{GreetingMessage, PersistentService}

import scala.concurrent.ExecutionContext

/**
  * Implementation of the TestakkaintegrationService.
  */
class PersistentServiceImpl(persistentEntityRegistry: PersistentEntityRegistry)
                           (implicit executionContext: ExecutionContext)  extends PersistentService {

  override def hello(id: String) = ServiceCall { _ =>
    val ref = persistentEntityRegistry.refFor[TestEntity](id)
    ref.ask(Hello(id))
  }

  override def useGreeting(id: String) = ServiceCall { request =>
    val ref = persistentEntityRegistry.refFor[TestEntity](id)
    ref.ask(UseGreetingMessage(request.message))
  }

  def getGreeting(id: String): ServiceCall[NotUsed, GreetingMessage] = ServiceCall { request =>
    val ref = persistentEntityRegistry.refFor[TestEntity](id)
    ref.ask(GetGreetingMessage).map(GreetingMessage(_))
  }


}
