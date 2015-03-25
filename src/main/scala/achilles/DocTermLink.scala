package achilles

import akka.actor.Actor
import akka.actor.Actor.Receive

class DocTermLink(val docId: Long, val termId: Long) extends Node with Actor {
  var currentTopic: Long = -1
  var currentTimeStamp: Long = -1

  override def nodeType(): String = ???

  override def compute(): Unit = ???

  def sampleOnce(): Long = {
    currentTimeStamp += 1
    ???
  }

  override def receive: Receive = {
    case SampleOneTopic(timestamp) =>
      if (timestamp < currentTimeStamp) {
        // invalid staled message
      } else if (timestamp == currentTimeStamp) {
        val prevTopic = sampleOnce()
        context.parent ! UpdateDocTopic(prevTopic, currentTopic, currentTimeStamp)
        context.parent ! UpdateTermTopic(prevTopic, currentTopic, currentTimeStamp)
      } else {
        context.parent ! GetDocTopic(docId, currentTopic, currentTimeStamp)
        context.parent ! GetTermTopic(termId, currentTopic, currentTimeStamp)
      }


  }

  override def timestamp(): Long = ???
}