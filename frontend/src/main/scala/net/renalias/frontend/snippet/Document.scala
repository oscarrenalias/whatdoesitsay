package net.renalias.frontend.snippet

import net.liftweb._
import util._
import Helpers._

import scala.xml.{NodeSeq, Text}
import net.liftweb.http._
import net.liftweb.common._

import net.renalias.frontend.model._
import net.renalias.frontend.snippet.document._

object document {

	object documentIdSessionVar extends SessionVar[Box[String]](Empty)

}

class document extends Logger {

	lazy val errorNotFound = (id: String) => Text("The document " + id + " could not be found")

	def documentInfo(xhtml: NodeSeq, docId: Box[String]): NodeSeq = {
		lazy val documentId = docId openOr (documentIdSessionVar.is openOr "")

		debug("Retrieving document with id = " + documentId)

		ScanJob.find(documentId) match {
			case Full(job) if (job.status.value == ScanJobStatus.New) => {
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