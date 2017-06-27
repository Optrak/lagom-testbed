package com.optrak.testakka.simple.impl

import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.optrak.testakka.impl.MockPersistentService
import com.optrak.testakka.simple.api.SimpleService
import org.scalatest.BeforeAndAfterAll

class SimpleServiceTest extends SimpleServiceTestBase with BeforeAndAfterAll  {

  override protected def afterAll() = server.stop()

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new SimpleServiceApplication(ctx) with LocalServiceLocator {
      override lazy val persistentService = new MockPersistentService
    }
  }

  override val simpleService = server.serviceClient.implement[SimpleService]

}
