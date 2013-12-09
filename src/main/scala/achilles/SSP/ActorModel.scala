/**
 *  Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package sample.remote.calculator

/*
 * comments like //#<tag> are there for inclusion into docs, please don’t remove
 */

import breeze.linalg._
import breeze.numerics._
import breeze.config.CommandLineParser
import breeze.util.Index
import chalk.text.transform.StopWordFilter
import breeze.util.Implicits._
import scala.collection.mutable.Map
import achilles.dataming.recommending.topics.{REC, smartcnTokenizer}

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

//#imports

class ActorModel extends Bootable {
  //#setup
  val system =
    ActorSystem("AdaptorApplication", ConfigFactory.load.getConfig("remotelookup"))
  val remotePath =
    "akka.tcp://CalculatorApplication@127.0.0.1:2552/user/simpleCalculator"
  val actor = system.actorOf(Props(classOf[AdaptorActor], remotePath), "lookupActor")

  system.scheduler.schedule(100 millis, 100 millis) {
    actor ! checkDB
  }

  def doSomething(op: RequireMsg): Unit =
    actor ! op
  //#setup

  def startup() {
  }

  def shutdown() {
    system.shutdown()
  }
}

//#actor
class AdaptorActor(path: String) extends Actor with ActorLogging {

  context.setReceiveTimeout(3.seconds)
  sendIdentifyRequest()

  def sendIdentifyRequest(): Unit =
    context.actorSelection(path) ! Identify(path)

  def receive = {
    case ActorIdentity(`path`, Some(actor)) =>
      context.setReceiveTimeout(Duration.Undefined)
      context.become(active(actor))
    case ActorIdentity(`path`, None) => println(s"Remote actor not availible: $path")
    case ReceiveTimeout              => sendIdentifyRequest()
    case _                           => println("Not ready yet")
  }

  def active(actor: ActorRef): Actor.Receive = {
    case feedTermWeight(tw) =>
    case feedTopicMixes(tm) =>
  }
}
//#actor

object ActorModel {
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
      while(k < numTopics) {
        ll +=  (topicSmoothing - 1)*(dig(k) - digsum) + lgamma(theta(k)) - (theta(k) - 1)*(dig(k) - digsum)
        var i = 0
        while(i < doc.activeSize) {
          val n = doc.indexAt(i)
          ll += doc.valueAt(i) * (gamma(k, i)*((dig(k) - digsum) - log(gamma(k, i)) + math.log(termWeights(k, n))))
          i += 1
        }

        k += 1
      }

      ll
    }
  }

  case class Params(dburl: String,
                    dbuser: String,
                    dbpasswd: String,
                    jdbcDriver: String = "com.mysql.jdbc.Driver",
                    numTopics: Int = 20,
                    topicSmoothing: Double = .1,
                    wordSmoothing: Double = 0.1)

  def main(args: Array[String]) = {
    val config = CommandLineParser.parseArguments(args)._1
    val params = config.readIn[Params]("")
    import params._

    // Feature map
    val fmap = Index[String]()
    val removeStopWords = new StopWordFilter("en")

    val buc = new BeanListUC(dburl, jdbcDriver, dbuser, dbpasswd)
    val uc: Map[String, String] = buc.getUC()
    val keys = uc.keys.toArray
    val values = uc.values.toArray

    // Read in the training data and index it.
    val almostTrainingData = for {
      text <- values
    } yield {
      val builder = new VectorBuilder[Double](Int.MaxValue, text.length / 20)
      for(tok <- smartcnTokenizer(text) if tok(0).isLetter && removeStopWords(tok)) {
        println(tok)
        builder.add(fmap.index(tok), 1.0)
      }
      builder
    }

    val trainingData = almostTrainingData.map{ b => b.length = fmap.size; b.toSparseVector}

    val rec = new REC(params.numTopics, params.topicSmoothing, params.wordSmoothing)

    val model = rec.iterations(trainingData).tee(m => println(m.likelihood)).last
    for( (list, k) <- model.topicMixes zip keys) {
      println("Doc %s:".format(k))
      println(list)
    }
    val topKLists = for(k <- 0 until numTopics) yield model.termWeights.t(::, k).argtopk(50).map(i => fmap.get(i) + " "+  model.termWeights(k, i))
    for( (list,k) <- topKLists.zipWithIndex) {
      println("Topic %d:".format(k))
      println(list.mkString("\t","\n\t", "\n"))
    }

    val app = new ActorModel
    println("Started Adaptor Application")
    app.doSomething(checkDB())  }
}