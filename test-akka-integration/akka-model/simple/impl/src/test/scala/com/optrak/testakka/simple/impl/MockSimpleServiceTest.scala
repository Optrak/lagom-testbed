package com.optrak.testakka.simple.impl

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.optrak.testakka.impl.MockPersistentService
import com.optrak.testakka.simple.api.SimpleService
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}

class MockSimpleServiceTest() extends SimpleServiceTestBase with BeforeAndAfterAll  {

  val system = ActorSystem("simpleServiceTest")

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val persistentService = new MockPersistentService

  override val simpleService = new SimpleServiceImpl(system, persistentService)

}
