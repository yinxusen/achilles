package achilles

/**
 * Created by panda on 3/24/15.
 */

import breeze.linalg._

trait Message

case class DocTopicDist(docTopic: SparseVector[Double]) extends Message

case class TermTopicDist(termTopic: SparseVector[Double]) extends Message

case class UpdateDocTopic(docId: Long, topicId: Long) extends Message
