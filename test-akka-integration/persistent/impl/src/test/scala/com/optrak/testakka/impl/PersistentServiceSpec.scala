package com.optrak.testakka.impl

import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.optrak.testakka.api.{GreetingMessage, PersistentService}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class PersistentServiceSpec extends PersistentServiceBase {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new PersistentApplication(ctx) with LocalServiceLocator
  }

  override lazy val client: PersistentService = server.serviceClient.implement[PersistentService]

  override protected def afterAll() = server.stop()
}
