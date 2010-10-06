package net.renalias.wdis.comet

import scala.actors.Actor
import scala.actors.Actor._
import net.liftweb.http.CometActor
import net.liftweb.http.S
import net.liftweb.http.ShutDown
import _root_.net.liftweb.util.Log
import _root_.net.liftweb.util.Helpers._
import _root_.scala.xml.{NodeSeq,Text,Node,Elem}
import _root_.net.liftweb.common.{Box,Full,Empty}
import net.liftweb.mapper._

import net.renalias.wdis.logger.SimpleLogger
import net.renalias.wdis.io._
import net.renalias.wdis.misc._
import net.renalias.wdis.model._

class ScanJobActor extends CometActor with SimpleLogger {
	
	override def defaultPrefix = Box("Job")
	
	// full re-render every time
	override def devMode = true	
	
	// to track whether the job has been completed or not
	var jobComplete = false
	
	var jobId = S.param("documentId").openOr("")
	//var jobId = "123"
	
	lazy val errorNotFound = Text("Job information not found")

	def render = {
		ScanJob.find(By(ScanJob.jobId, jobId)) match {
			case Full(job) if(job.status.is != ScanJobStatus.Completed) => {
				log.debug("Job " + jobId + "isn't ready yet, setting up the actor...")
				ScanJobMonitor ! AddJobListener(jobId, this)
				Text("job: " + job.jobId.is + " - status: " + job.status.is)
			}
			case Full(job) if(job.status.is == ScanJobStatus.Completed) => {
				log.debug("Job " + jobId + "is already completed, not need to set up the actor")
				Text("job: " + job.jobId.is + " - status: " + job.status.is + "-text:" + job.text.is)
			}
			case _ => errorNotFound
		}
	}
	
	override def localSetup = {
		log.debug("Stating comet actor")		
		
		/*ScanJob.find(By(ScanJob.jobId, jobId)) match {
			case Full(job) if(job.status.is != ScanJobStatus.Completed) => {
				log.debug("Job " + jobId + "isn't ready yet, setting up the actor...")
				ScanJobMonitor ! AddJobListener(jobId, this)
				Text("job: " + job.jobId.is + " - status: " + job.status.is)
				this ! ShutDown
			}
			case Full(job) if(job.status.is == ScanJobStatus.Completed) => {
				log.debug("Job " + jobId + "is already completed, not need to set up the actor")
				Text("job: " + job.jobId.is + " - status: " + job.status.is + "-text:" + job.text.is)
			}
			case _ => errorNotFound
		}*/
		ScanJobMonitor ! AddJobListener(jobId, this)
	}
	
	override def localShutdown = {
		log.debug("Shutting down comet actor")
		ScanJobMonitor ! RemoveJobListener(jobId, this)
	}
	
	override def lowPriority = {
		case JobCompleted(jobId) => {
			log.debug("Job " + jobId + " notified as complete")
			jobComplete = true
			reRender(true)
			this ! ShutDown
		}
		case _ => log.error("ScanJobActor got a message that did not understand")
	}
}