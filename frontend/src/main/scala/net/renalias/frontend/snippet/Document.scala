package net.renalias.frontend.snippet

import _root_.net.liftweb._
import http._
import util._
import Helpers._

import _root_.scala.xml.{NodeSeq, Text, Group}
import _root_.net.liftweb.http._
import _root_.net.liftweb.common._

import net.renalias.frontend.model._

class Document extends Logger {

	lazy val errorNotFound = Text("The document could not be found")

	def documentInfo(xhtml: NodeSeq, docId: Box[String]): NodeSeq = {
		docId match {
			case Full(docId) => {
				ScanJob.find(docId) match {
					case Full(job) if (job.status.value != ScanJobStatus.Completed) => {
						info("docId = " + docId)
						<lift:comet type="ScanJobActor" name={docId}/>
					}
					case Full(job) if (job.status.value == ScanJobStatus.Completed) => {
						bind("document", xhtml, "id" -> job.id.value.get, "status" -> job.status.value.toString, "text" -> job.text.value.getOrElse(""))
					}
					case _ => errorNotFound
				}
			}
			case _ => errorNotFound
		}
	}

	def showDocument(xhtml: NodeSeq) = documentInfo(xhtml, S.param("documentId"))
}