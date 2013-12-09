/**
 * Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package achilles.SSP

/*
 * comments like //#<tag> are there for inclusion into docs, please don’t remove
 */

import breeze.linalg._
import breeze.config.CommandLineParser
import breeze.util.Index
import chalk.text.transform.StopWordFilter
import scala.collection.mutable.Map
import achilles.dataming.recommending.topics.smartcnTokenizer

//--------------------------------------------------------
// easy for bean

import achilles.util.BeanListUC
import collection.JavaConversions._

import com.typesafe.config.ConfigFactory
import akka.actor._
import akka.kernel.Bootable

class ModelActor(params: ModelActor.Params, trainingData: IndexedSeq[SparseVector[Double]]) extends Bootable {
  //#setup
  val system =
    ActorSystem("AdaptorApplication", ConfigFactory.load.getConfig("remotelookup"))
  val remotePath =
    "akka.tcp://CalculatorApplication@127.0.0.1:2552/user/simpleCalculator"
  val parallelBlock = 10
  val numTopics = 10
  val numWords = trainingData.head.size
  val oneBlockCount = (trainingData.length / parallelBlock).toInt
  val dataBlocks = for (i <- 0 until parallelBlock) yield trainingData.zipWithIndex.slice(i * oneBlockCount, if ((i + 1) * oneBlockCount > trainingData.length) trainingData.length else (i + 1) * oneBlockCount)
  val actors = for (i <- 0 until parallelBlock) yield system.actorOf(Props(classOf[ModelTrainer], remotePath, params, dataBlocks(i), numWords, numTopics, dataBlocks(i).length), "lookupActor")

  def bootstrap(counts: Int): Unit = {
    for (i <- 0 until counts) {
      for (actor <- actors) {
        actor ! requestTermWeight
        actor ! requestTopicMixes
      }
    }
  }

  def startup() {
  }

  def shutdown() {
    system.shutdown()
  }
}

object ModelActor {

  case class Params(dburl: String,
                    dbuser: String,
                    dbpasswd: String,
                    jdbcDriver: String = "com.mysql.jdbc.Driver",
                    numTopics: Int = 20,
                    topicSmoothing: Double = .1,
                    wordSmoothing: Double = 0.1)

  def main(args: Array[String]) = {
    val config = CommandLineParser.parseArguments(args)._1
    val params = config.readIn[Params]("")
    import params._

    // Feature map
    val fmap = Index[String]()
    val removeStopWords = new StopWordFilter("en")

    val buc = new BeanListUC(dburl, jdbcDriver, dbuser, dbpasswd)
    val uc: Map[String, String] = buc.getUC()
    val keys = uc.keys.toArray
    val values = uc.values.toArray

    // Read in the training data and index it.
    val almostTrainingData = for {
      text <- values
    } yield {
      val builder = new VectorBuilder[Double](Int.MaxValue, text.length / 20)
      for (tok <- smartcnTokenizer(text) if tok(0).isLetter && removeStopWords(tok)) {
        println(tok)
        builder.add(fmap.index(tok), 1.0)
      }
      builder
    }

    val trainingData = almostTrainingData.map {
      b => b.length = fmap.size; b.toSparseVector
    }

    val app = new ModelActor(params, trainingData)
    app.bootstrap(10)

    /*
    val rec = new TopicModel(params.numTopics, params.topicSmoothing, params.wordSmoothing)

    val model = rec.iterations(trainingData).tee(m => println(m.likelihood)).last
    for( (list, k) <- model.topicMixes zip keys) {
      println("Doc %s:".format(k))
      println(list)
    }
    val topKLists = for(k <- 0 until numTopics) yield model.termWeights.t(::, k).argtopk(50).map(i => fmap.get(i) + " "+  model.termWeights(k, i))
    for( (list,k) <- topKLists.zipWithIndex) {
      println("Topic %d:".format(k))
      println(list.mkString("\t","\n\t", "\n"))
    }
    */
  }
}
