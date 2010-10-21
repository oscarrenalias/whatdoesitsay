package net.renalias.wdis.frontend.comet

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

import net.renalias.wdis.common.logger.SimpleLogger
import net.renalias.wdis.common.io._
import net.renalias.wdis.frontend.misc._
import net.renalias.wdis.frontend.model._

class ScanJobActor extends CometActor with SimpleLogger {
	
	override def defaultPrefix = Box("Job")
	
	// full re-render every time
	override def devMode = true	
	
	// to track whether the job has been completed or not
	var jobComplete = false

	// lifespan of the comet actor
	override def lifespan = Full(5 minutes)	
	
	lazy val errorNotFound = Text("Job information not found")

	// retrieves the job identifier
	def jobId = name match {
		case Empty => log.warning("Empty jobId for actor!"); "" 
		case Full(x) => log.debug("Actor jobId: " + x); x
	}

	def render = {
		ScanJob.find(By(ScanJob.jobId, jobId)) match {
			case Full(job) if(job.status.is != ScanJobStatus.Completed) => {
				log.debug("Job " + jobId + "isn't ready yet, setting up the actor...")
				Text("job: " + job.jobId.is + " - status: " + job.status.is)
			}
			case Full(job) if(job.status.is == ScanJobStatus.Completed) => {
				//log.debug("Job " + jobId + "is already completed, not need to set up the actor")
				//Text("job: " + job.jobId.is + " - status: " + job.status.is + "-text:" + job.text.is)
				<lift:embed what="/templates-hidden/job-data" />
			}
			case _ => errorNotFound
		}
	}
	
	override def localSetup = {
		log.debug("Starting comet actor: " + {jobId})		
		ScanJobMonitor ! AddJobListener(jobId, this)
	}
	
	override def localShutdown = {
		log.debug("Shutting down comet actor: " + {jobId})
		ScanJobMonitor ! RemoveJobListener(jobId, this)		
	}
	
	override def lowPriority = {
		case JobCompleted(jobId) => {
			log.debug("Job " + jobId + " notified as complete")
			jobComplete = true
			reRender(devMode)			
			ScanJobMonitor ! RemoveJobListener(jobId, this)			
		}
		case _ => log.error("ScanJobActor got a message that did not understand")
	}
}