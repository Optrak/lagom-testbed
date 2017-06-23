package com.optrak.testakka.impl

import java.time.LocalDateTime

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.optrak.testakka.api.GreetingMessage
import com.optrak.testakka.utils.JsonFormats
import play.api.libs.json.{Format, Json}

import scala.collection.immutable.Seq

/**
  * This is an event sourced entity. It has a state, [[TestEntityState]], which
  * stores what the greeting should be (eg, "Hello").
  *
  * Event sourced entities are interacted with by sending them commands. This
  * entity supports two commands, a [[UseGreetingMessage]] command, which is
  * used to change the greeting, and a [[Hello]] command, which is a read
  * only command which returns a greeting to the name specified by the command.
  *
  * Commands get translated to events, and it's the events that get persisted by
  * the entity. Each event will have an event handler registered for it, and an
  * event handler simply applies an event to the current state. This will be done
  * when the event is first created, and it will also be done when the entity is
  * loaded from the database - each event will be replayed to recreate the state
  * of the entity.
  *
  * This entity defines one event, the [[GreetingMessageChanged]] event,
  * which is emitted when a [[UseGreetingMessage]] command is received.
  */
class TestEntity extends PersistentEntity {

  override type Command = TestEntityCommand[_]
  override type Event = TestEntityEvent
  override type State = TestEntityState

  /**
    * The initial state. This is used if there is no snapshotted state to be found.
    */
  override def initialState: TestEntityState = TestEntityState("Hello", LocalDateTime.now.toString)

  override def behavior: Behavior = {
    case TestEntityState(message, _) => Actions().onCommand[UseGreetingMessage, Done] {

      case (UseGreetingMessage(newMessage), ctx, state) =>
        ctx.thenPersist(
          GreetingMessageChanged(newMessage)
        ) { _ =>
          ctx.reply(Done)
        }
    }.onReadOnlyCommand[GetGreetingMessage.type, String] {
      case (GetGreetingMessage, ctx, state) =>
        ctx.reply(state.message)
    }.onReadOnlyCommand[Hello, String] {
      case (Hello(name), ctx, state) =>
        ctx.reply(s"$message, $name!")
    }.onEvent {
      case (GreetingMessageChanged(newMessage), state) =>
        TestEntityState(newMessage, LocalDateTime.now().toString)
    }
  }
}

/**
  * The current state held by the persistent entity.
  */
case class TestEntityState(message: String, timestamp: String)

object TestEntityState {
  implicit val format: Format[TestEntityState] = Json.format
}
sealed trait TestEntityEvent extends AggregateEvent[TestEntityEvent] {
  def aggregateTag = TestEntityEvent.Tag
}

object TestEntityEvent {
  val Tag = AggregateEventTag[TestEntityEvent]
}

case class GreetingMessageChanged(message: String) extends TestEntityEvent

object GreetingMessageChanged {

  implicit val format: Format[GreetingMessageChanged] = Json.format
}
sealed trait TestEntityCommand[R] extends ReplyType[R]
case class UseGreetingMessage(message: String) extends TestEntityCommand[Done]

object UseGreetingMessage {
  implicit val format: Format[UseGreetingMessage] = Json.format
}

case object GetGreetingMessage extends TestEntityCommand[String] {
  implicit val format: Format[GetGreetingMessage.type] = JsonFormats.singletonFormat(GetGreetingMessage)
}



case class Hello(name: String) extends TestEntityCommand[String]

object Hello {
  implicit val format: Format[Hello] = Json.format
}

object PersistentServiceSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[UseGreetingMessage],
    JsonSerializer[Hello],
    JsonSerializer[GreetingMessageChanged],
    JsonSerializer[GetGreetingMessage.type],
    JsonSerializer[TestEntityState]
  )
}
