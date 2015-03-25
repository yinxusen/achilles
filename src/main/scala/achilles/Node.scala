package achilles

/**
 * Created by panda on 3/24/15.
 */

import akka.actor.Actor
import akka.actor.Actor.Receive
import breeze.linalg._

trait Node {
  def nodeType(): String
  def compute(): Unit
}

class DocNode extends Node with Actor {
  var docTopic: SparseVector[Double] = ???
  override def nodeType(): String = "doc"
  override def compute(): Unit = ???

  def randomTopic(numTopics: Long): SparseVector[Double] = {
    ???
  }
  override def receive: Receive = {
    case RandomTopic(numTopics) => randomTopic(numTopics)
    case UpdateDocTopic(topicId) =>
      ???
    case _ =>
      ???
  }
}

class TermNode(val termId: Long) extends Node {
  var termTopic: SparseVector[Double] = ???
  override def nodeType(): String = "term"
  override def compute(): Unit = ???
}

class TopicNode(val topicId: Long) extends Node {
  var topicCount: Int = ???
  override def nodeType(): String = "topic"
  override def compute(): Unit = ???
}

class DocTermLink(val docId: Long, val termId: Long) extends Node {
  var topicId: Long = 0

  override def nodeType(): String = ???

  override def compute(): Unit = ???
}

class TermTopicLink(val termId: Long, val topicId: Long) extends Node {
  var topicCount: Long = 0

  override def nodeType(): String = ???

  override def compute(): Unit = ???
}
