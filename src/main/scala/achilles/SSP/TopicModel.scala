package achilles.SSP

import breeze.linalg._
import breeze.numerics._

//--------------------------------------------------------
// easy for bean


class TopicModel(numTopics: Int, topicSmoothing: Double = 0.5, wordSmoothing: Double = 0.05, numIterations: Int = 5) {

  import TopicModel._

  // def fitModel(data: IndexedSeq[SparseVector[Double]]): Model = iterations(data).drop(1).take(numIterations).last

  def iterations(data: IndexedSeq[SparseVector[Double]], termWeights: DenseMatrix[Double], topicMixes: Array[DenseVector[Double]]): Iterator[Model] = {
    val numWords = data.head.size
    val numDocs = data.size

    Iterator.iterate(Model(termWeights, topicMixes, 0.0, numTopics, topicSmoothing, wordSmoothing)) {
      current =>
        var ll = 0.0
        val counts = data.par.aggregate(null: DenseMatrix[Double])({
          (_counts, doc) =>
            val counts = Option(_counts).getOrElse(DenseMatrix.zeros[Double](numTopics, numWords))
            val result = current.inference(doc)
            val gamma = result.wordLoadings
            // sum up expected counts
            var i = 0
            ll += result.ll
            while (i < doc.activeSize) {
              counts(::, doc.indexAt(i)) += gamma(::, i) * doc.valueAt(i)
              i += 1
            }
            counts
        }, {
          _ += _
        })

        val topics = data.map {
          current.inference(_).topicLoadings
        }.toArray

        // m step: Beta = exp(digamma(counts) - digamma(\sum(counts))
        counts += topicSmoothing
        val newCounts = digamma(counts)
        for (k <- 0 until numTopics) {
          newCounts(k, ::) -= digamma(sum(counts(k, ::)))
        }

        // compute the rest of the likelihood (from the word counts)
        ll += numTopics * (lgamma(wordSmoothing * numWords) - numWords * lgamma(wordSmoothing))
        ll += (wordSmoothing - 1) * (newCounts.sum)
        ll -= ((counts - 1.0) :* (newCounts)).sum
        ll += lbeta(counts, Axis._1).sum

        exp.inPlace(newCounts)
        current.copy(newCounts, topics, ll)
    }

  }.drop(1).take(numIterations)
}

object TopicModel {

  case class Model(termWeights: DenseMatrix[Double], topicMixes: Array[DenseVector[Double]], likelihood: Double, numTopics: Int, topicSmoothing: Double, wordSmoothing: Double) {

    case class InferenceResult(topicLoadings: DenseVector[Double], wordLoadings: DenseMatrix[Double], ll: Double)

    def inference(doc: SparseVector[Double]) = {
      var converged = false
      var iter = 25
      val gamma = DenseMatrix.zeros[Double](numTopics, doc.activeSize)
      gamma := topicSmoothing
      var alpha = DenseVector.fill(numTopics)(topicSmoothing)
      var newAlpha = DenseVector.zeros[Double](numTopics)
      var ll = 0.0

      // inference
      while (!converged && iter > 0) {
        converged = true
        newAlpha := topicSmoothing
        iter -= 1
        var i = 0
        while (i < doc.activeSize) {
          val result = normalize(alpha :* termWeights(::, doc.indexAt(i)), 1)
          assert(!norm(result).isNaN, gamma(::, i).toString + " " + alpha.toString + " " + termWeights(::, doc.indexAt(i)))

          converged &&= norm(gamma(::, i) - result, Double.PositiveInfinity) < 1E-4
          gamma(::, i) := result
          newAlpha += (result * doc.valueAt(i))
          i += 1
        }
        val newLL = likelihood(doc, newAlpha, gamma)
        ll = newLL

        if (!converged) {
          val xx = newAlpha
          newAlpha = alpha
          alpha = xx
          digamma.inPlace(alpha)
          exp.inPlace(alpha)
        }

      }
      InferenceResult(alpha, gamma, ll)
    }

    private def likelihood(doc: SparseVector[Double], theta: DenseVector[Double], gamma: DenseMatrix[Double]) = {
      val dig = digamma(theta)
      val digsum = digamma(sum(theta))
      var ll = lgamma(topicSmoothing * numTopics) - numTopics * lgamma(topicSmoothing) - lgamma(sum(theta))
      var k = 0
      while (k < numTopics) {
        ll += (topicSmoothing - 1) * (dig(k) - digsum) + lgamma(theta(k)) - (theta(k) - 1) * (dig(k) - digsum)
        var i = 0
        while (i < doc.activeSize) {
          val n = doc.indexAt(i)
          ll += doc.valueAt(i) * (gamma(k, i) * ((dig(k) - digsum) - log(gamma(k, i)) + math.log(termWeights(k, n))))
          i += 1
        }

        k += 1
      }

      ll
    }
  }

}

// vim: set ts=4 sw=4 et:
