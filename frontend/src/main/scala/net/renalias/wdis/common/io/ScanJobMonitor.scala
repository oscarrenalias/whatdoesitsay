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
	
	// register as a listener with the folder watcher
	FolderWatcher.start
	FolderWatcher.addListener(this)
	
	def getJobIdFromFile(jobFile:String): String =
		jobFile.indexOf(".") match {
			case -1 => jobFile
			case x:Int => jobFile.substring(0, x)
		}
		
	def getJobText(jobId:String): String = {
		val fileName = Config.getString("folders.completed", ".") + jobId + ".txt"
		var fileContents = ""
		
		for {  
		    (line) <- Source.fromFile(fileName).getLines  
		} fileContents += line		

		debug("job:" + jobId + " - text:" + fileContents)
		
		fileContents
	}
	
	def processJob(jobFile:String) = {
		val jobId = getJobIdFromFile(jobFile)
		debug("Processing job = " + jobFile)
		
		// update the job in the database
		//ScanJob.find(By(ScanJob.jobId, jobId)) match {
		ScanJob.fetch(jobId) match {
			case Full(job) => {
				// get the contents of the text file and update the status
				//job.status.set(ScanJobStatus.Completed).
				job.status.set(ScanJobStatus.Completed)
				job.text.set(Full(getJobText(jobId)))
				job.completedDate(Calendar.getInstance)			
				job.save 
				// notify listeners
				notify(JobCompleted(jobId))
			}
			case _ => error("There was no matching job with id = " + jobId)
		}
		
		// and notify the comet actor who may be waiting for this
		//TBD
	}
	
	def act() = { 
		loop {
			react {
				case FilesAdded(files) => {
					debug("FilesAdded message received")
					files.foreach(f => processJob(f))
				}
				case FilesRemoved(_) => debug("FilesRemoved message received")
				// messages to add and remove listeners
				case AddJobListener(jobId, s) => {
					debug("Adding listener for jobId: " + jobId)
					addListener(s)
				}
				case RemoveJobListener(jobId, s) => {
					debug("Removing listener for jobId: " + jobId)
					removeListener(s)
				}
				case _ => debug("We can ignore this message")
			}
		}
	}
}