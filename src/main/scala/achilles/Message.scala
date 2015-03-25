package achilles

/**
 * Created by panda on 3/24/15.
 */

import breeze.linalg._

trait Message

case class DocTopicDist(docTopic: SparseVector[Double]) extends Message

case class TermTopicDist(termTopic: SparseVector[Double]) extends Message

case class UpdateDocTopic(from: Long, to: Long, timestamp: Long) extends Message
case class UpdateTermTopic(from: Long, to: Long, timestamp: Long) extends Message
case class GetDocTopic(docId: Long, topicId: Long, timestamp: Long) extends Message
case class GetTermTopic(termId: Long, topicId: Long, timestamp: Long) extends Message
case class SampleOneTopic(timestamp: Long) extends Message

case class RandomTopic(numTopics: Long) extends Message

case class ConsumeSourceBlock(sourceBlock: Iterator[Iterable[(String, Int)]]) extends Message