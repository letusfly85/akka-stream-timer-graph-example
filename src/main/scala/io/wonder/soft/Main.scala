package io.wonder.soft

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import io.wonder.soft.graph.{SampleTimerFlow, SampleTimerSource}

import scala.concurrent.ExecutionContextExecutor


object Main {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem  = ActorSystem("FileStream")
    implicit val executor: ExecutionContextExecutor = system.dispatcher
    implicit val materializer: Materializer = ActorMaterializer()

    val source = Source.fromGraph(new SampleTimerSource)
    val flow = Flow.fromGraph(new SampleTimerFlow)

    val runnableGraph = source.via(flow).toMat(Sink.foreach(println))(Keep.left) //.to(Sink.foreach(println))

    runnableGraph.run()
  }

}
