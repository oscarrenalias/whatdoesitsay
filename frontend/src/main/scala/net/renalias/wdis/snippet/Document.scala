package net.renalias.wdis.snippet

import _root_.net.liftweb._
import http._
import mapper._
import S._
import SHtml._

import common._
import util._
import Helpers._

import _root_.scala.xml.{NodeSeq, Text, Group}
import _root_.net.liftweb.http._
import _root_.net.liftweb.wizard._
import _root_.net.liftweb.common._

import net.renalias.wdis.logger.SimpleLogger
import net.renalias.wdis.io.FileHelper._
import net.renalias.wdis.model._
import net.renalias.wdis.config._

class Document {
	
	def showDocument: NodeSeq = {
		
		lazy val errorNotFound = Text("The document could not be found")
		
		S.param("documentId") match {
			case Full(docId) => {
				// load the job from the db
				ScanJob.find(By(ScanJob.jobId, docId)) match {
					case Full(job) if(job.status.is != ScanJobStatus.Completed) => {
						 <lift:comet type="ScanJobActor" />
					}
					case Full(job) if(job.status.is == ScanJobStatus.Completed) => Text("job: " + job.jobId.is + " - status: " + job.status.is + "-text:" + job.text.is)
					case _ => errorNotFound
				}
			}
			case _ => errorNotFound
		}
	}
}