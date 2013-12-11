/**
 * Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package achilles.SSP

import breeze.linalg._

trait ServerMsg

trait WorkerMsg

case class UpdateTermWeight(updater: DenseMatrix[Double]) extends ServerMsg

case class UpdateTopicMixes(updater: Array[DenseVector[Double]], idx: Array[Int]) extends ServerMsg

case object RequestTermWeight extends ServerMsg

case class RequestTopicMixes(idx: Array[Int]) extends ServerMsg

case class FeedTermWeight(termWeight: DenseMatrix[Double]) extends WorkerMsg

case class FeedTopicMixes(topicMixes: Array[DenseVector[Double]]) extends WorkerMsg

case object StartFetchTermWeight extends WorkerMsg

case object StartFetchTopicMixes extends WorkerMsg

case object ReportLL extends ServerMsg
