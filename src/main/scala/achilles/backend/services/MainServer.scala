/**
 *  Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package achilles.backend.services

/*
 * comments like //#<tag> are there for inclusion into docs, please don’t remove
 */

import akka.kernel.Bootable
import akka.actor.{ Props, Actor, ActorSystem }
import com.typesafe.config.ConfigFactory

//#actor
class MainServerActor extends Actor {
  def receive = {
    case QueryRecom(uid, content, location) =>
    case Add(n1, n2) ⇒
      println("Calculating %d + %d".format(n1, n2))
      sender ! AddResult(n1, n2, n1 + n2)
    case Subtract(n1, n2) ⇒
      println("Calculating %d - %d".format(n1, n2))
      sender ! SubtractResult(n1, n2, n1 - n2)
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
