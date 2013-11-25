/**
 *  Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package achilles.backend.services

/*
 * comments like //#<tag> are there for inclusion into docs, please don’t remove
 */

import akka.kernel.Bootable
import akka.actor._
import com.typesafe.config.ConfigFactory
import achilles.dataming.recommending.topics.{dbRec, streamingRec}
import achilles.backend.services.QueryRecom

//#actor
class MainServerActor(streamActor: ActorRef, dbActor: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case QueryRecom(uid, content, location) =>
      log.info("Query recommendation from Rec Actor")
      streamActor ! QueryRecom(uid, content, location)
      dbActor ! QueryRecom(uid, content, location)
  }
}
//#actor

class MainServerApp extends Bootable {
  //#setup
  val system = ActorSystem("MainServerApp",
    ConfigFactory.load.getConfig("mainserver"))
  val actor = system.actorOf(Props[MainServerActor], "MainServerActor")
  //#setup

  def startup() {
  }

  def shutdown() {
    system.shutdown()
  }
}

object CalcApp {
  def main(args: Array[String]) {
    new MainServerApp
    println("Started Calculator Application - waiting for messages")
  }
}
