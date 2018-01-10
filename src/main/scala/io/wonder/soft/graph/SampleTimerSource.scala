package io.wonder.soft.graph

import java.util.{Calendar, Date}

import akka.stream.{Attributes, Outlet, SourceShape}
import akka.stream.stage.{AbstractOutHandler, GraphStage, TimerGraphStageLogic}

import scala.collection.mutable
import scala.concurrent.duration._

class SampleTimerSource extends GraphStage[SourceShape[String]] {

  val out: Outlet[String] = Outlet("Date.out")
  override val shape: SourceShape[String] = SourceShape(out)

  def tickTackDate(date: Date): String = date.toString
  var date = new Date
  val calendar: Calendar  = Calendar.getInstance()

  override def createLogic(inheritedAttributes: Attributes): TimerGraphStageLogic = {
    calendar.setTime(date)

    val que = new mutable.Queue[Date]()
    new TimerGraphStageLogic(shape) {
      setHandler(out, new AbstractOutHandler {
        override def onPull(): Unit = {
          scheduleOnce("init", 1000.milliseconds)
        }
      })

      override def onTimer(timerKey: Any): Unit = {
        if (que.size < 10) {
          println(s"queueing data... ${que.size}")

          calendar.add(Calendar.SECOND, 1)
          date = calendar.getTime
          que.enqueue(date)

          scheduleOnce("poll", 1000.milliseconds)

        }  else if (que.size > 11) {
          push(out, tickTackDate(que.head))
          que.dequeue()

        } else {
          calendar.add(Calendar.SECOND, 1)
          date = calendar.getTime
          que.enqueue(date)

          scheduleOnce("poll", 1000.milliseconds)
        }
      }

      override def preStart(): Unit = {
        super.preStart()
      }
    }
  }

}
