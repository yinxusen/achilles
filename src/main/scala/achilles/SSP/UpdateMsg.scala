/**
 *  Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package achilles.SSP

import breeze.linalg._

trait ServerMsg
trait WorkerMsg

case class updateTermWeight(updater: DenseMatrix[Double]) extends ServerMsg
case class updateTopicMixes(updater: DenseVector[Double], idx: Int) extends ServerMsg
case class requestTermWeight() extends ServerMsg
case class requestTopicMixes() extends ServerMsg

case class feedTermWeight(termWeight: DenseMatrix[Double]) extends WorkerMsg
case class feedTopicMixes(topicMixes: Array[DenseVector[Double]]) extends WorkerMsg