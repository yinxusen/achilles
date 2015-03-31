package achilles

import breeze.linalg.SparseVector

/**
 * Created by panda on 3/31/15.
 */
class TermNode(val identity: String) extends Node {
  var termTopic: SparseVector[Double] = ???
  override def nodeType(): String = "term"
  override def compute(): Unit = ???

  override def timestamp(): Long = ???
}

