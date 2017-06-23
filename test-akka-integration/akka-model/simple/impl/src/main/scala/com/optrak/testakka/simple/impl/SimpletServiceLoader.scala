package com.optrak.testakka.simple.impl

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
import com.optrak.testakka.simple.api.SimpleService
import com.softwaremill.macwire.{wire, _}

import scala.concurrent.ExecutionContext

trait SimpleServiceComponents extends LagomServerComponents
  with CassandraPersistenceComponents
  with LagomServiceClientComponents {
  implicit def executionContext: ExecutionContext

  implicit lazy val persistentService = serviceClient.implement[PersistentService]
  override lazy val lagomServer = serverFor[SimpleService](wire[SimpleServiceImpl])
  override lazy val jsonSerializerRegistry = SimpleServiceSerializerRegistry
}

class SimpleServiceLoader extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext): LagomApplication =
    new SimpleServiceApplication(context)    {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }
  override def describeServices = List(readDescriptor[SimpleService]  )

  override def loadDevMode(context: LagomApplicationContext): LagomApplication = {
    new SimpleServiceApplication(context ) with LagomDevModeComponents
  }
}


abstract class SimpleServiceApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with SimpleServiceComponents
    with CassandraPersistenceComponents
    with AhcWSComponents 
