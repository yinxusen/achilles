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

  def timestamp(): Long
}



class TermNode(val identity: String) extends Node {
  var termTopic: SparseVector[Double] = ???
  override def nodeType(): String = "term"
  override def compute(): Unit = ???

  override def timestamp(): Long = ???
}

class TopicNode(val topicId: Long) extends Node {
  var topicCount: Int = ???
  override def nodeType(): String = "topic"
  override def compute(): Unit = ???

  override def timestamp(): Long = ???
}



class TermTopicLink(val termId: Long, val topicId: Long) extends Node {
  var topicCount: Long = 0

  override def nodeType(): String = ???

  override def compute(): Unit = ???

  override def timestamp(): Long = ???
}
