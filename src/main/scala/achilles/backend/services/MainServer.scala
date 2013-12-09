/**
 *  Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package achilles.SSP

/*
 * comments like //#<tag> are there for inclusion into docs, please don’t remove
 */

import akka.actor.{ActorSystem, ActorLogging, Actor, ActorRef, Props}
import akka.kernel.Bootable
import akka.pattern._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.concurrent.Future

//#actor
class MainServerActor(streamActor: ActorRef, dbActor: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case QueryRecom(uid, content, location) =>
      log.info("Query recommendation from Rec Actor")

      implicit val timeout = Timeout(1.second)
      val streamFuture = streamActor ? QueryRecom(uid, content, location)
      val dbFuture = dbActor ? QueryRecom(uid, content, location)
      val requester = sender

      streamFuture map {
        requester.!
      } recover {
        case _ => dbFuture map {
          requester.!
        } recover {
          case _ => log.info("Time out both in streaming and database recommendation")
        }
      }
  }
}
//#actor

class MainServerApp extends Bootable {
  //#setup
  val system = ActorSystem("MainServerApp",
    ConfigFactory.load.getConfig("mainserver"))
  val actor = system.actorOf(Props[ServerActor], "MainServerActor")
  //#setup

  def startup() {
  }

  def shutdown() {
    system.shutdown()
  }
}

object RecApp {
  def main(args: Array[String]) {
    new MainServerApp
    println("Started Calculator Application - waiting for messages")
  }
}
