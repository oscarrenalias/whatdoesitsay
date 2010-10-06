package net.renalias.wdis.io

import _root_.net.liftweb._
import mapper._
import common._
import util._
import Helpers._

import scala.io.Source
import scala.actors._

import java.util.Date

import net.renalias.wdis.misc._
import net.renalias.wdis.model._
import net.renalias.wdis.io._
import net.renalias.wdis.logger.SimpleLogger
import net.renalias.wdis.config.Config
import net.renalias.wdis.comet.ScanJobActor

case class AddJobListener(val jobId:String, val s:ScanJobActor)
case class RemoveJobListener(val jobId:String, val s:ScanJobActor)
case class JobCompleted(val jobId:String)

object ScanJobMonitor extends ListenerManager with SimpleLogger with Actor {
	
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
		
		log.debug("job:" + jobId + " - text:" + fileContents)
		
		fileContents
	}
	
	def processJob(jobFile:String) = {
		val jobId = getJobIdFromFile(jobFile)
		log.debug("Processing job = " + jobFile)
		
		// update the job in the database
		ScanJob.find(By(ScanJob.jobId, jobId)) match {
			case Full(job) => {
				// get the contents of the text file and update the status
				job.status(ScanJobStatus.Completed).
				text(getJobText(jobId)).
				completedDate(new Date).
				save 
				// notify listeners
				notify(JobCompleted(jobId))
			}
			case _ => log.error("There was no matching job with id = " + jobId)
		}
		
		// and notify the comet actor who may be waiting for this
		//TBD
	}
	
	def act() = { 
		loop {
			react {
				case FilesAdded(files) => {
					log.debug("FilesAdded message received")
					files.foreach(f => processJob(f))
				}
				case FilesRemoved(_) => log.debug("FilesRemoved message received")
				// messages to add and remove listeners
				case AddJobListener(jobId, s) => {
					log.debug("Adding listener for jobId: " + jobId)
					addListener(s)
				}
				case RemoveJobListener(jobId, s) => {
					log.debug("Removing listener for jobId: " + jobId)
					removeListener(s)
				}
				case _ => log.debug("We can ignore this message")
			}
		}
	}
}