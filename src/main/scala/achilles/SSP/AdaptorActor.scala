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

class AdaptorActor(path: String, params: ActorModel.Params, trainingData: IndexedSeq[(SparseVector[Double], Int)], numWords: Int, numTopics: Int, numDocs: Int) extends Actor with ActorLogging {
  import ActorModel._
  context.setReceiveTimeout(3.seconds)
  sendIdentifyRequest()

  val rec = new LDA(params.numTopics, params.topicSmoothing, params.wordSmoothing)

  var lastTermWeights = DenseMatrix.rand(numTopics, numWords) / numWords.toDouble
  var lastTopicMixes = new Array[DenseVector[Double]](numDocs)

  val indexes = trainingData.map {_.2}.toArray
  val dataset = trainingData.map {_.1}

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

  def runNTimes(tw: DenseMatrix[Double]): Model = {
    val indexedTopicMixes = lastTopicMixes.slice(indexes)
    rec.iterations(dataset, tw, indexedTopicMixes).tee(m => println(m.likelihood)).last
  }

  def runNTimes(tm: Array[DenseVector[Double]]): Model = {
    val indexedTopicMixes = tm.slice(indexes)
    rec.iterations(dataset, lastTermWeights, indexedTopicMixes).tee(m => println(m.likelihood)).last
  }

  def active(actor: ActorRef): Actor.Receive = {
    case feedTermWeight(tw) =>
      newModel = runNTimes(tw)
      sender !
    case feedTopicMixes(tm) =>
      newModel = runNTimes(tm)
  }
}

// vim: set ts=4 sw=4 et:
