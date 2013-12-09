package achilles.SSP

import breeze.linalg._
import breeze.numerics._
import breeze.config.CommandLineParser
import breeze.util.Index
import chalk.text.transform.StopWordFilter
import breeze.util.Implicits._
import scala.collection.mutable.Map
import achilles.dataming.recommending.topics.smartcnTokenizer

//--------------------------------------------------------
// easy for bean
import achilles.util.BeanListUC
import collection.JavaConversions._

import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import akka.actor._
import akka.kernel.Bootable
import achilles.backend.services._
import achilles.backend.services.checkDB
import akka.actor.ActorIdentity
import scala.Some
import akka.actor.Identify
import scala.concurrent._
import ExecutionContext.Implicits.global
import achilles.SSP.{feedTopicMixes, feedTermWeight}


class LDA(numTopics: Int, topicSmoothing: Double = 0.5, wordSmoothing: Double = 0.05, numIterations: Int = 5) {
  import ActorModel._


  // def fitModel(data: IndexedSeq[SparseVector[Double]]): Model = iterations(data).drop(1).take(numIterations).last

  def iterations(data: IndexedSeq[SparseVector[Double]], termWeights: DenseMatrix[Double], topicMixes: Array[DenseVector[Double]]): Iterator[Model] = {
    val numWords = data.head.size
    val numDocs = data.size

    Iterator.iterate(Model(termWeights, topicMixes, 0.0, numTopics, topicSmoothing, wordSmoothing)) { current =>
      var ll = 0.0
      val counts = data.par.aggregate(null:DenseMatrix[Double])({ ( _counts, doc) =>
        val counts =  Option(_counts).getOrElse(DenseMatrix.zeros[Double](numTopics, numWords))
        val result = current.inference(doc)
        val gamma = result.wordLoadings
        // sum up expected counts
        var i = 0
        ll += result.ll
        while(i < doc.activeSize) {
          counts(::, doc.indexAt(i)) += gamma(::, i) * doc.valueAt(i)
          i += 1
        }
        counts
      }, {_ += _})

      val topics = data.map {
        current.inference(_).topicLoadings
      }.toArray

      // m step: Beta = exp(digamma(counts) - digamma(\sum(counts))
      counts += topicSmoothing
      val newCounts =  digamma(counts)
      for(k <- 0 until numTopics) {
        newCounts(k, ::) -= digamma(sum(counts(k,::)))
      }

      // compute the rest of the likelihood (from the word counts)
      ll += numTopics * (lgamma(wordSmoothing * numWords) - numWords * lgamma(wordSmoothing))
      ll += (wordSmoothing-1) * (newCounts.sum)
      ll -= ((counts - 1.0) :* (newCounts)).sum
      ll += lbeta(counts, Axis._1).sum

      exp.inPlace(newCounts)
      current.copy(newCounts, topics, ll)
    }

  }.drop(1).take(numIterations)
}

// vim: set ts=4 sw=4 et:
