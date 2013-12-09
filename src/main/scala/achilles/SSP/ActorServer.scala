/**
 *  Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package achilles.SSP

/*
 * comments like //#<tag> are there for inclusion into docs, please don’t remove
 */

import akka.actor.{ActorSystem, ActorLogging, Actor, ActorRef, Props}
import akka.kernel.Bootable
import com.typesafe.config.ConfigFactory
import breeze.linalg.{DenseVector, DenseMatrix}

//#actor
class ActorServer(numTopics: Int, numWords: Int, numDocs: Int) extends Actor with ActorLogging {
  var termWeight: DenseMatrix[Double] = DenseMatrix.rand(numTopics, numWords) / numWords.toDouble
  var topicMixes: Array[DenseVector[Double]] = new Array[DenseVector[Double]](numDocs)

  def receive = {
    case updateTermWeight(tw) =>
      termWeight += tw // Here need more details
    case updateTopicMixes(tm, idx) =>
      for((id, j) <- idx.zipWithIndex) topicMixes.update(j, tm(id))
    case requestTermWeight =>
      sender ! feedTermWeight(termWeight)
    case requestTopicMixes(idx) =>
      sender ! feedTopicMixes(idx map {topicMixes(_)})
  }
}
//#actor

class MainServerApp extends Bootable {
  //#setup
  val system = ActorSystem("MainServerApp",
    ConfigFactory.load.getConfig("mainserver"))
  val actor = system.actorOf(Props[ActorServer], "MainServerActor")
  //#setup

  def startup() {
  }

  def shutdown() {
    system.shutdown()
  }
}

object ActorServer {
  def main(args: Array[String]) {
    new MainServerApp
    println("Started Calculator Application - waiting for messages")
  }
}
