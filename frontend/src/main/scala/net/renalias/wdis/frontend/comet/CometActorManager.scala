package net.renalias.wdis.frontend.comet

import _root_.net.liftweb._
import common.Logger
import scala.actors._

import net.renalias.wdis.frontend.misc._

case class AddJobListener(val jobId:String, val s:ScanJobActor)
case class RemoveJobListener(val jobId:String, val s:ScanJobActor)
case class JobCompleted(val jobId:String)

trait CometActorManagerComponent {

	class CometActorManager extends ListenerManager with Logger with Actor {

		info("Starting CometActorManager")
		start

		override def notify(message: Any) = {
			info("Notifying receivers of message:" + message)
			super.notify(message)
		}

		def act() = {
			loop {
				react {
					case AddJobListener(jobId, s) => {
						info("Adding listener for jobId: " + jobId)
						addListener(s)
					}
					case RemoveJobListener(jobId, s) => {
						info("Removing listener for jobId: " + jobId)
						removeListener(s)
					}
					case _ => debug("We can ignore this message")
				}
			}
		}
	}

	object CometActorManager extends CometActorManager
}