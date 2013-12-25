/**
 * Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package achilles.SSP

/*
 * comments like //#<tag> are there for inclusion into docs, please don’t remove
 */

import com.typesafe.config.ConfigFactory
import akka.actor._
import akka.kernel.Bootable

import breeze.linalg._
import breeze.config.CommandLineParser
import breeze.util.Index
import java.io.File
import chalk.text.tokenize.JavaWordTokenizer
import scala.io._
import akka.event.Logging
import scala.util.Random

class ModelActor(params: ModelActor.Params, trainingData: IndexedSeq[SparseVector[Double]]) extends Bootable {
  //#setup
  val system =
    ActorSystem("ModelActor", ConfigFactory.load.getConfig("modelactor"))
  val log = system.log
  val remotePath = "akka.tcp://ServerActorApp@127.0.0.1:2552/user/ServerActor"

  val staleness = 4
  val parallelBlock = 3
  val numTopics = params.numTopics
  val numWords = trainingData.head.size
  val oneBlockCount = trainingData.length / parallelBlock
  val dataBlocks =
    for (i <- 0 until parallelBlock)
      yield trainingData.zipWithIndex.slice(i * oneBlockCount,
        if ((i + 1) * oneBlockCount > trainingData.length) trainingData.length
        else (i + 1) * oneBlockCount)

  log.info("number of words: {}", numWords)
  log.info("number of docs: {}", trainingData.length)
  log.info("one block counts: {}", oneBlockCount)
  log.info("length of data blocks: {}", dataBlocks.length)
  log.info("data blocks first block count: {}", dataBlocks.head.length)

  val actors =
    for (i <- 0 until parallelBlock) yield
      system.actorOf(ModelTrainer
        .props(remotePath, params, dataBlocks(i), numWords, numTopics, dataBlocks(i).length, staleness), "workers-"+i)

  def bootstrap(counts: Int): Unit = {
    for (i <- 0 until counts) {
      for (actor <- actors) {
        // Thread.sleep(10000)
        if(Random.nextBoolean) actor ! StartFetchTermWeight else actor ! StartFetchTopicMixes
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

  case class Params(dir: File = new File("/home/sen/test"),
                    numTopics: Int = 20,
                    topicSmoothing: Double = .1,
                    wordSmoothing: Double = 0.1)

  def main(args: Array[String]) = {
    val config = CommandLineParser.parseArguments(args)._1
    val params = config.readIn[Params]("")
    import params._

    // Feature map
    val fmap = Index[String]()


    // val removeStopWords = new StopWordFilter("en")
    // Read in the training data and index it.
    val almostTrainingData = for {
      f <- dir.listFiles
    } yield {
      val text = Source.fromFile(f)("UTF-8").mkString
      val builder = new VectorBuilder[Double](Int.MaxValue, text.length / 20)
      for(tok <- JavaWordTokenizer(text) if tok(0).isLetter) {
        builder.add(fmap.index(tok), 1.0)
      }

      builder
    }

    val trainingData = almostTrainingData.map {
      b: VectorBuilder[Double] =>
        b.length = fmap.size
        b.toSparseVector
    }

    new ServerActorApp(params.numTopics, trainingData.head.size, trainingData.length)
    println("server start up...")
    val app = new ModelActor(params, trainingData)
    println("worker start up...")
    app.bootstrap(10)

    /*
    val rec = new TopicModel(params.numTopics, params.topicSmoothing, params.wordSmoothing)
                                                  Double] =
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
