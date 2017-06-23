package com.optrak.testakka.impl

import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.optrak.testakka.api.{GreetingMessage, PersistentService}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class PersistentServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new PersistentApplication(ctx) with LocalServiceLocator
  }

  val client = server.serviceClient.implement[PersistentService]

  override protected def afterAll() = server.stop()

  "test-akka-integration service" should {

    "say hello" in {
      client.hello("Alice").invoke().map { answer =>
        answer should ===("Hello, Alice!")
      }
    }

    "allow responding with a custom message" in {
      for {
        _ <- client.useGreeting("Bob").invoke(GreetingMessage("Hi"))
        answer <- client.hello("Bob").invoke()
      } yield {
        answer should ===("Hi, Bob!")
      }
    }
    "gest custom message" in {
      for {
        msg <- client.getGreeting("Bob").invoke()
      } yield {
        msg should ===(GreetingMessage("Hi"))
      }
    }
  }
}
