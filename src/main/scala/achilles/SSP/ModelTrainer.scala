package achilles.SSP

import breeze.linalg._
import scala.concurrent.duration._
import akka.actor._
import breeze.util.Implicits._
import scala.util.Random
import akka.util.Timeout

class ModelTrainer(
    path: String,
    params: ModelActor.Params,
    trainingData: IndexedSeq[(SparseVector[Double], Int)],
    numWords: Int,
    numTopics: Int,
    numDocs: Int,
    iterations: Int = 10,
    staleness: Int = 1)
  extends Actor with ActorLogging {

  import TopicModel._

  override def preStart() {
    sendIdentifyRequest()
    implicit val timeout = Timeout(3.seconds)

    context.actorSelection(path).resolveOne() map {
      case ActorIdentity(_, Some(actor)) =>
        log.info(s"${this.getClass.getName} get ready!")
        context.become(active(actor))
        bootstrap(iterations * 2)
    } recover {
      case t: Throwable =>
        log.error(s"Remote actor not available: $path")
    }
  }

  def bootstrap(counts: Int): Unit = {
    for (i <- 0 until counts) {
        if(Random.nextBoolean) self ! StartFetchTermWeight else self ! StartFetchTopicMixes
    }
  }

  val rec = new TopicModel(params.numTopics, params.topicSmoothing, params.wordSmoothing)

  var lastTermWeights = DenseMatrix.rand(numTopics, numWords) / numWords.toDouble
  var lastTopicMixes = new Array[DenseVector[Double]](numDocs)

  val indexes = trainingData.map(x => x._2).toArray
  val dataset = trainingData.map(x => x._1)

  log.debug(s"Number of docs: $numDocs")
  log.debug(s"indexes: $indexes")
  log.debug(s"number of dataset: ${dataset.length}")

  def sendIdentifyRequest(): Unit =
    context.actorSelection(path) ! Identify(path)

  def receive = {
    case _ => log.info(s" ${this.getClass.getName} not ready yet")
  }

  def runNTimes(tw: DenseMatrix[Double]): Model = {
    lastTermWeights = tw
    val numIterations = Random.nextInt(staleness) + 1
    log.info(s"Iterate $numIterations times in termWeight changed.")
    rec.iterations(dataset, tw, lastTopicMixes, numIterations).tee(m => println(m.likelihood)).last
  }

  def runNTimes(tm: Array[DenseVector[Double]]): Model = {
    lastTopicMixes = tm
    val numIterations = Random.nextInt(staleness) + 1
    log.info(s"Iterate $numIterations times in topic mix changed.")
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
      log.info(s"feedTermWeight to $sender")
      val newModel = runNTimes(tw)
      sender ! UpdateTermWeight(newModel.termWeights)
      sender ! UpdateTopicMixes(newModel.topicMixes, indexes)
    case FeedTopicMixes(tm) =>
      log.info(s"feedTopicMixes to $sender")
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
      numDocs: Int,
      iterations: Int,
      staleness: Int) =
    Props(classOf[ModelTrainer], path, params, trainingData, numWords, numTopics, numDocs, iterations, staleness)
}

// vim: set ts=4 sw=4 et:
