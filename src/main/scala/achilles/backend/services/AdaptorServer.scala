/**
 *  Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package sample.remote.calculator

/*
 * comments like //#<tag> are there for inclusion into docs, please don’t remove
 */

import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import akka.actor._
import akka.kernel.Bootable
import akka.actor.ActorIdentity
import scala.Some
import akka.actor.Identify
import achilles.backend.services._
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import achilles.frontend.query.server.sqldb.DB
import achilles.backend.services.checkDB
import achilles.backend.services.BasicRecResult
import akka.actor.ActorIdentity
import scala.Some
import akka.actor.Identify
import achilles.frontend.query.server.publish.weibo.PublishWeibo
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

//#imports

class AdaptorApplication extends Bootable {
  //#setup
  val system =
    ActorSystem("AdaptorApplication", ConfigFactory.load.getConfig("remotelookup"))
  val remotePath =
    "akka.tcp://CalculatorApplication@127.0.0.1:2552/user/simpleCalculator"
  val actor = system.actorOf(Props(classOf[AdaptorActor], remotePath), "lookupActor")

  system.scheduler.schedule(100 millis, 100 millis) {
    actor ! checkDB
  }

  def doSomething(op: RequireMsg): Unit =
    actor ! op
  //#setup

  def startup() {
  }

  def shutdown() {
    system.shutdown()
  }
}

//#actor
class AdaptorActor(path: String) extends Actor with ActorLogging {

  context.setReceiveTimeout(3.seconds)
  sendIdentifyRequest()

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

  def active(actor: ActorRef): Actor.Receive = {
    case msg: checkDB => {
      DB.getLatestStatus map {
        case s if (s.length == 3) => actor ! QueryRecom(s(0), s(1), s(2))
      }
    }
    case BasicRecResult(a, b, c) =>
      // publish weibo
      future {
        PublishWeibo.comment("123","123","123")
      } onComplete {
        case Success(r) => log.info("commit to someone")
        case Failure(e) => log.error("failed to comment")
      }
  }
}
//#actor

object AdaptorApp {
  def main(args: Array[String]) {
    val app = new AdaptorApplication
    println("Started Adaptor Application")
    app.doSomething(checkDB())
  }
}