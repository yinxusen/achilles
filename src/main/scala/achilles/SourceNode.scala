package achilles

import scala.collection.immutable.HashMap
import scala.util.Success
import scala.util.Failure

import akka.actor.{ActorRef, Props, Actor}

import achilles.util.help

class SourceNode extends Node with Actor {
  var routerTable: HashMap[String, ActorRef] = HashMap.empty[String, ActorRef]

  def findOrCreateNode[T](name: String): Unit = {
    if (!routerTable.contains(name)) {
      context.actorSelection(name).resolveOne().onComplete {
        case Success(actor) => routerTable += ((name, actor))
        case Failure(ex) =>
          val newRef = context.actorOf(Props[T], name)
          routerTable += ((name, newRef))
      }
    }
  }

  override def nodeType(): String = "source"
  override def compute(): Unit = ???

  def compute(sourceBlock: Iterator[Iterable[(String, Int)]]): Unit = {
    sourceBlock.map { source =>
      val docIdentity = s"doc-$help.docHash(source)"
      findOrCreateNode[DocNode](docIdentity)
      source.map { case (term, count) =>
        val termIdentity = s"term-${help.termHash(term)}"
        findOrCreateNode[TermNode](termIdentity)
        val docTermLinkIdentity = s"doc-term-$docIdentity-$termIdentity"
        findOrCreateNode[DocTermLink](docTermLinkIdentity)
      }
    }
  }

  override def receive: Receive = {
    case ConsumeSourceBlock(sourceBlock) => compute()
    case _ => ()
  }
}
