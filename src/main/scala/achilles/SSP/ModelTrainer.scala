package achilles.SSP

import breeze.linalg._
import scala.concurrent.duration._
import akka.actor._
import breeze.util.Implicits._
import scala.util.Random

class ModelTrainer(
    path: String,
    params: ModelActor.Params,
    trainingData: IndexedSeq[(SparseVector[Double], Int)],
    numWords: Int,
    numTopics: Int,
    numDocs: Int,
    staleness: Int = 1)
  extends Actor with ActorLogging {

  import TopicModel._

  override def preStart() {
    context.setReceiveTimeout(3.seconds)
    sendIdentifyRequest()
  }

  val rec = new TopicModel(params.numTopics, params.topicSmoothing, params.wordSmoothing)

  var lastTermWeights = DenseMatrix.rand(numTopics, numWords) / numWords.toDouble
  var lastTopicMixes = new Array[DenseVector[Double]](numDocs)

  val indexes = trainingData.map(x => x._2).toArray
  val dataset = trainingData.map(x => x._1)

  log.info("number of docs: {}", numDocs)
  log.info("indexes: {}", indexes)
  log.info("number of dataset: {}", dataset.length)

  def sendIdentifyRequest(): Unit =
    context.actorSelection(path) ! Identify(path)

  def receive = {
    case ActorIdentity(`path`, Some(actor)) =>
      println("get ready!")
      context.setReceiveTimeout(Duration.Undefined)
      context.become(active(actor))
    case ActorIdentity(`path`, None) => println(s"Remote actor not availible: $path")
    case ReceiveTimeout => sendIdentifyRequest()
    case _ => println("Not ready yet")
  }

  def runNTimes(tw: DenseMatrix[Double]): Model = {
    lastTermWeights = tw
    val numIterations = Random.nextInt(staleness)
    rec.iterations(dataset, tw, lastTopicMixes, numIterations).tee(m => println(m.likelihood)).last
  }

  def runNTimes(tm: Array[DenseVector[Double]]): Model = {
    lastTopicMixes = tm
    val numIterations = Random.nextInt(staleness)
    rec.iterations(dataset, lastTermWeights, tm, numIterations).tee(m => println(m.likelihood)).last
  }

  def active(actor: ActorRef): Actor.Receive = {
    case StartFetchTopicMixes =>
      log.info("ask for topic mixes")
      actor ! RequestTopicMixes(indexes)
    case StartFetchTermWeight =>
      log.info("ask for term weights")
      actor ! RequestTermWeight
    case FeedTermWeight(tw) =>
      log.info("feedTermWeight to {}", sender)
      val newModel = runNTimes(tw)
      sender ! UpdateTermWeight(newModel.termWeights)
      sender ! UpdateTopicMixes(newModel.topicMixes, indexes)
    case FeedTopicMixes(tm) =>
      log.info("feedTopicMixes to {}", sender)
      val newModel = runNTimes(tm)
      sender ! UpdateTermWeight(newModel.termWeights)
      sender ! UpdateTopicMixes(newModel.topicMixes, indexes)
  }
}

object ModelTrainer {
  def props(
      path: String,
      params: ModelActor.Params,
      trainingData: IndexedSeq[(SparseVector[Double], Int)],
      numWords: Int,
      numTopics: Int,
      numDocs: Int) =
    Props(classOf[ModelTrainer], path, params, trainingData, numWords, numTopics, numDocs)
}

// vim: set ts=4 sw=4 et:
