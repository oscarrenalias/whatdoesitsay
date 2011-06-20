package net.renalias.frontend.comet

import net.liftweb.http.CometActor
import _root_.net.liftweb.util.Helpers._
import _root_.scala.xml.{Text}
import net.liftweb.common.{Logger, Box, Full, Empty}
import net.renalias.frontend.model.{ScanJobStatus, ScanJob}

class ScanJobActor extends CometActor with Logger {
	
	override def defaultPrefix = Box(Some("Job"))

	// full re-render every time
	override def devMode = true

	// to track whether the job has been completed or not
	var jobComplete = false

	// lifespan of the comet actor
	override def lifespan = Full(5 minutes)

	lazy val errorNotFound = Text("Job information not found")

	// retrieves the job identifier
	def jobId = name match {
		case Full(x) => x
		case _ => error("Empty jobId for actor!"); ""
	}

	def render = {
		ScanJob.find(jobId) match {
			case Full(job) if(job.status.is != ScanJobStatus.Completed) => {
				info("Job " + jobId + "isn't ready yet, setting up the actor...")
				Text("job: " + job.id.is + " - status: " + job.status.is)
			}
			case Full(job) if(job.status.is == ScanJobStatus.Completed) => {
				//log.debug("Job " + jobId + "is already completed, not need to set up the actor")
				//Text("job: " + job.jobId.is + " - status: " + job.status.is + "-text:" + job.text.is)
				info("Job compelted - returning full template")
				<lift:embed what="/templates-hidden/job-data" />
			}
			case _ => errorNotFound
		}
	}

	override def localSetup = {
		info("Starting comet actor: " + {jobId})
		CometActorManager ! AddJobListener(jobId, this)
	}

	override def localShutdown = {
		info("Shutting down comet actor: " + {jobId})
		CometActorManager ! RemoveJobListener(jobId, this)
	}

	override def lowPriority = {
		case JobCompleted(jobId) => {
			info("Job " + jobId + " notified as complete")
			jobComplete = true
			reRender(devMode)
			CometActorManager ! RemoveJobListener(jobId, this)
		}
		case _ => error("ScanJobActor got a message that did not understand")
	}
}