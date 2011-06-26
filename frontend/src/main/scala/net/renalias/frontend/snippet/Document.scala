package net.renalias.frontend.snippet

import _root_.net.liftweb._
import http._
import util._
import Helpers._

import scala.xml.{NodeSeq, Text, Group}
import net.liftweb.http._
import net.liftweb.common._

import net.renalias.frontend.model._

// the document identifier is initially empty
object documentIdRequestVar extends RequestVar[Box[String]](Empty)

/**
 * Note to self: when using the HTML5 parser, snippets must be in lower case
 */
class document extends Logger {

	lazy val errorNotFound = (id:String) => Text("The document " + id + " could not be found")

	def documentInfo(xhtml: NodeSeq, docId: Box[String]): NodeSeq = {
    lazy val documentId = {
      if(docId.isEmpty) debug("docId from the request is empty")
      docId openOr (documentIdRequestVar.is openOr "")
    }

    debug("documentId = " + documentId)

  				ScanJob.find(documentId) match {
					case Full(job) if (job.status.value == ScanJobStatus.New) => {
						info("documentId = " + documentId)
						<lift:comet type="ScanJobActor" name={documentId}/>
					}
					case Full(job) if (job.status.value == ScanJobStatus.Completed) => {
						bind("document", xhtml, "id" -> job.id.is.toString, "status" -> job.status.value.toString, "text" -> job.text.value.getOrElse(""))
					}
					case _ => errorNotFound(documentId)
				}
	}

	def showdocument(xhtml: NodeSeq) = documentInfo(xhtml, S.param("documentId"))
}