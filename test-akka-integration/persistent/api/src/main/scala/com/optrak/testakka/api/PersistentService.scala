package com.optrak.testakka.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}

trait PersistentService extends Service {

  def hello(id: String): ServiceCall[NotUsed, String]

  def useGreeting(id: String): ServiceCall[GreetingMessage, Done]

  def getGreeting(id: String): ServiceCall[NotUsed, GreetingMessage]

  override final def descriptor = {
    import Service._
    named("persistent")
      .withCalls(
        pathCall("/persistent/hello/:id", hello _),
        pathCall("/persistent/greeting/:id", useGreeting _),
        pathCall("/persistent/greeting/:id", getGreeting _)
      )
      .withAutoAcl(true)
  }
}
case class GreetingMessage(message: String)

object GreetingMessage {
  implicit val format: Format[GreetingMessage] = Json.format[GreetingMessage]
}
