package net.renalias.wdis.common.io

import _root_.net.liftweb._
import common.{Full, Logger => LiftLogger}
import util._

import scala.io.Source
import scala.actors._

import java.util.Calendar

import net.renalias.wdis.frontend.misc._
import net.renalias.wdis.frontend.model._
import net.renalias.wdis.common.config.Config
import net.renalias.wdis.frontend.comet.ScanJobActor


case class AddJobListener(val jobId:String, val s:ScanJobActor)
case class RemoveJobListener(val jobId:String, val s:ScanJobActor)
case class JobCompleted(val jobId:String)

object ScanJobMonitor extends ListenerManager with LiftLogger with Actor {
	
	def act() = { 
		loop {
			react {
				// messages to add and remove listeners
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