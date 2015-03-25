package achilles

import akka.actor.Actor
import breeze.linalg.SparseVector

class DocNode(val identity: String) extends Node with Actor {
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

  override def timestamp(): Long = ???
}
