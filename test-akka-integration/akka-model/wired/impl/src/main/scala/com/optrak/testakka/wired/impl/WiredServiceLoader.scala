package com.optrak.testakka.wired.impl

import com.lightbend.lagom.internal.client.CircuitBreakerMetricsProviderImpl
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomApplicationLoader, LagomServerComponents, _}
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.client.LagomServiceClientComponents
import com.optrak.testakka.api.PersistentService
import com.optrak.testakka.wired.api.WiredService
import com.optrak.testakka.wired.impl.model.{ModelManagerRef, ModelModule}
import com.softwaremill.macwire.{wire, _}

import scala.concurrent.ExecutionContext

trait WiredServiceComponents extends LagomServerComponents
  with CassandraPersistenceComponents
  with LagomServiceClientComponents
with ModelModule {
  implicit def executionContext: ExecutionContext

  override lazy val persistentService = serviceClient.implement[PersistentService]
  override lazy val lagomServer = serverFor[WiredService](wire[WiredServiceImpl])
  override lazy val jsonSerializerRegistry = WiredServiceSerializerRegistry
}

class WiredServiceLoader extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext): LagomApplication =
    new WiredServiceApplication(context)    {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }
  override def describeServices = List(readDescriptor[WiredService]  )

  override def loadDevMode(context: LagomApplicationContext): LagomApplication = {
    new WiredServiceApplication(context ) with LagomDevModeComponents
  }
}


abstract class WiredServiceApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with WiredServiceComponents
    with CassandraPersistenceComponents
    with AhcWSComponents 
