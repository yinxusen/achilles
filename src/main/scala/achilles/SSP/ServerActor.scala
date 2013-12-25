/**
 * Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package achilles.SSP

import akka.actor.{ActorSystem, ActorLogging, Actor, Props}
import akka.kernel.Bootable
import com.typesafe.config.ConfigFactory
import breeze.linalg.{DenseVector, DenseMatrix}

class ServerActor(numTopics: Int, numWords: Int, numDocs: Int) extends Actor with ActorLogging {
  var termWeight: DenseMatrix[Double] = DenseMatrix.rand(numTopics, numWords) / numWords.toDouble
  var topicMixes: Array[DenseVector[Double]] = new Array[DenseVector[Double]](numDocs)

  def receive = {
    case UpdateTermWeight(tw) =>
      log.info("update term weight from {}", sender)
      termWeight += tw // Here need more details
    case UpdateTopicMixes(tm, idx) =>
      log.info("update topic mixes from {}", sender)
      log.info("updated idx: {}", idx)
      for ((id, j) <- idx.zipWithIndex) topicMixes.update(id, tm(j))
    case RequestTermWeight =>
      log.info("send term weight to {}", sender)
      sender ! FeedTermWeight(termWeight)
    case RequestTopicMixes(idx) =>
      log.info("send topic mixes to {}", sender)
      log.info("requested ids: {}", idx)
      sender ! FeedTopicMixes(idx map {
        topicMixes(_)
      })
    case ReportLL =>
      log.info("Report likelihood to master.")

  }
}

class ServerActorApp(numTopics: Int, numWords: Int, numDocs: Int) extends Bootable {
  //#setup
  val system = ActorSystem("ServerActorApp", ConfigFactory.load.getConfig("serveractor"))
  val actor = system.actorOf(Props(classOf[ServerActor], numTopics, numWords, numDocs), "ServerActor")
  //#setup

  def startup() {
  }

  def shutdown() {
    system.shutdown()
  }
}

object ServerActor {
}
