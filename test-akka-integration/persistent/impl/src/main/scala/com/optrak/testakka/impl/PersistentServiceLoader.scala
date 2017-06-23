package com.optrak.testakka.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.optrak.testakka.api.PersistentService
import com.softwaremill.macwire._

class PersistentServiceLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new PersistentApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new PersistentApplication(context) with LagomDevModeComponents

  override def describeServices = List(
    readDescriptor[PersistentService]
  )
}

abstract class PersistentApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[PersistentService](wire[PersistentServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = PersistentServiceSerializerRegistry

  // Register the test-akka-integration persistent entity
  persistentEntityRegistry.register(wire[TestEntity])
}
