package com.optrak.testakka.simple.impl

import com.optrak.testakka.api.GreetingMessage
import com.optrak.testakka.simple.api.SimpleService
import org.scalatest.{AsyncWordSpec, Matchers}

trait SimpleServiceTestBase extends AsyncWordSpec with Matchers {

  def simpleService: SimpleService

  "test-akka-integration service" should {

    "say hello" in {
      simpleService.hello("Alice").invoke().map { answer =>
        answer should ===("Hello, Alice!")
      }
    }

    "allow responding with a custom message" in {
      for {
        _ <- simpleService.useGreeting("Bob").invoke(GreetingMessage("Hi"))
        answer <- simpleService.hello("Bob").invoke()
      } yield {
        answer should ===("Hi, Bob!")
      }
    }
  }
}
