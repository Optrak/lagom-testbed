package com.optrak.testakka.impl

import com.optrak.testakka.api.{GreetingMessage, PersistentService}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

trait PersistentServiceBase extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  def client: PersistentService

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
