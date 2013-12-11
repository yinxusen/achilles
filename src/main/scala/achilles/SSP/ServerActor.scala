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
      println("update term weight from " + sender.toString)
      termWeight += tw // Here need more details
    case UpdateTopicMixes(tm, idx) =>
      println("update topic mixes from " + sender.toString)
      for ((id, j) <- idx.zipWithIndex) topicMixes.update(j, tm(id))
    case RequestTermWeight =>
      println("send term weight to " + sender.toString)
      sender ! FeedTermWeight(termWeight)
    case RequestTopicMixes(idx) =>
      println("send topic mixes to " + sender.toString)
      sender ! FeedTopicMixes(idx map {
        topicMixes(_)
      })
    case ReportLL =>

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
