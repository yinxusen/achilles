package achilles.SSP

import breeze.linalg._
import scala.concurrent.duration._
import akka.actor._
import akka.actor.ActorIdentity
import scala.Some
import akka.actor.Identify

class AdaptorActor(path: String, params: ActorModel.Params, trainingData: IndexedSeq[(SparseVector[Double], Int)], numWords: Int, numTopics: Int, numDocs: Int) extends Actor with ActorLogging {
  import ActorModel._
  context.setReceiveTimeout(3.seconds)
  sendIdentifyRequest()

  val rec = new LDA(params.numTopics, params.topicSmoothing, params.wordSmoothing)

  var lastTermWeights = DenseMatrix.rand(numTopics, numWords) / numWords.toDouble
  var lastTopicMixes = new Array[DenseVector[Double]](numDocs)

  val indexes = trainingData.map(_.2).toArray
  val dataset = trainingData.map(_.1)

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
    rec.iterations(dataset, tw, indexedTopicMixes).last
  }

  def runNTimes(tm: Array[DenseVector[Double]]): Model = {
    val indexedTopicMixes = tm.slice(indexes)
    rec.iterations(dataset, lastTermWeights, indexedTopicMixes).last
  }

  def active(actor: ActorRef): Actor.Receive = {
    case feedTermWeight(tw) =>
      newModel = runNTimes(tw)
      sender ! updateTermWeight(newModel.termWeights)
      sender ! updateTopicMixes(newModel.topicMixes, indexes)

    case feedTopicMixes(tm) =>
      newModel = runNTimes(tm)
      sender ! updateTermWeight(newModel.termWeights)
      sender ! updateTopicMixes(newModel.topicMixes, indexes)
  }
}

// vim: set ts=4 sw=4 et:
