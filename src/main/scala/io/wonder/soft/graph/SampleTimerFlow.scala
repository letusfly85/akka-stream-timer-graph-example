package io.wonder.soft.graph

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage._

import scala.collection.mutable
import scala.concurrent.duration._

class SampleTimerFlow extends GraphStage[FlowShape[String, String]] {
  val in: Inlet[String] = Inlet("String.in")
  val out: Outlet[String] = Outlet("String.out")

  val que = new mutable.Queue[String]()

  override val shape: FlowShape[String, String] = FlowShape(in, out)

  override def createLogic(inheritedAttributes: Attributes): TimerGraphStageLogic = {
    new TimerGraphStageLogic(shape) {

      // This requests one element at the Sink startup.
      override def preStart(): Unit = {
        pull(in)
      }

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          que.enqueue(grab(in))
          pull(in)
        }

        override def onUpstreamFinish(): Unit = {
          println(isAvailable(out))
          if (isAvailable(out)) Thread.sleep(1000L) // there is a pending element to emit
          else completeStage()
        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          if (que.size > 3) {
            push(out, que.head)
            que.dequeue()

          } else {
            scheduleOnce("init", 4.seconds)
          }
        }

        override def onDownstreamFinish(): Unit = {
          println(isAvailable(out))
          if (isAvailable(out)) Thread.sleep(1000L) // there is a pending element to emit
          else completeStage()
        }
      })

      override def onTimer(timerKey: Any): Unit = {
        if (que.size > 3) {
          push(out, que.head)
          que.dequeue()

        } else {
          scheduleOnce("poll", 4.seconds)
        }
      }
    }
  }

}

