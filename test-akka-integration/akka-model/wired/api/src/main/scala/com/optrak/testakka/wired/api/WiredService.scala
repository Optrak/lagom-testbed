package com.optrak.testakka.wired.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}
import com.optrak.testakka.api.GreetingMessage
/**
  * facade to persistent service using akka
  */
trait WiredService extends Service {

  def hello(id: String): ServiceCall[NotUsed, String]

  def useGreeting(id: String): ServiceCall[GreetingMessage, Done]

  override final def descriptor = {
    import Service._
    // @formatter:off
    named("wired")
      .withCalls(
        pathCall("/wired/hello/:id", hello _),
        pathCall("/wired/hello/:id", useGreeting _)
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}

