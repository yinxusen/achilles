package achilles.util

import breeze.linalg.{DenseVector, SparseVector}

import scala.collection.mutable
import scala.util.Random

/**
 * Created by sen on 12/29/13.
 */
object help {
  @annotation.tailrec
  def retry[T](n: Int)(fn: => T): util.Try[T] = {
    util.Try { fn } match {
      case x: util.Success[T] => x
      case _ if n > 1 => retry(n - 1)(fn)
      case f => f
    }
  }

  def termHash(s: String): Long = {
    ???
  }

  def docHash(d: Iterable[(String, Int)]): Long = {
    ???
  }

  def randomSparseVector(numTopic: Long): SparseVector[Double] = {
    ???
  }

  def generateAlias(prob: List[Double], len: Int): (List[Double], List[Int]) = {
    val mutableProb = mutable.MutableList(prob: _*)
    val k = mutableProb.zipWithIndex.map(_._2)
    val v = mutableProb.zipWithIndex.map(x => (x._2 + 1.0) / len)
    for (i <- 0 until len - 1) {
      val (_, biggestPos) = mutableProb.zipWithIndex.maxBy(_._1)
      val (smallestVal, smallestPos) = mutableProb.zipWithIndex.minBy(_._1)
      k(i) = biggestPos
      v(i) = (i - 1.0) / len + smallestVal
      mutableProb(biggestPos) -= smallestVal
      mutableProb(smallestPos) = 1.0 / len
    }
    (v.toList, k.toList)
  }

  def SampleAlias(k: List[Int], v: List[Double], len: Int): Int = {
    val bin = Random.nextDouble()
    val index = (len * bin).round.toInt
    if (bin < v(index))
      index
    else
      k(index)
  }
}
