package net.renalias.wdis.frontend.snippet

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite

import net.renalias.wdis.frontend.model._
import net.renalias.wdis.frontend.model.ScanJobStatus._
import net.liftweb.common.Full

import _root_.scala.xml.{NodeSeq, Text, Group}

class DocumentTest extends FunSuite with ShouldMatchers {

	def genTestJob(status:ScanJobStatus) = {
		var job = ScanJob.createRecord
		job.originalFileName.set("test.jpg")
		job.internalFileName.set("test.jpg")
		job.status.set(status)
		job.lang.set(ScanJobLang.ENG)
		job.save
		job
	}

	test("The Document snippet returns a Comet actor if the ScanJob is in status New") {
		val job = genTestJob(ScanJobStatus.New)
		val jobId = job.id.value.get
		val doc = new Document
		val result = doc.documentInfo(
			<lift:Document.showDocument>Document id: <document:id /><br/>Status: <document:status /><br/>Text:<br/><document:text /></lift:Document.showDocument>,
			Full(jobId)
		)

		result should equal(<lift:comet type="ScanJobActor" name="{jobId}"></lift:comet>)

		// delete the scanjob
		job.delete_!
	}

	test("The Document snippet returns an error if the ScanJob does not exist") {	
		val doc = new Document
		val result = doc.documentInfo(
			<lift:Document.showDocument>Document id: <document:id /><br/>Status: <document:status /><br/>Text:<br/><document:text /></lift:Document.showDocument>,
			Full("whatever")
		)

		result should equal(Text("The document could not be found"))
	}

	test("The Document snippet returns full data if the document is in status Ready") {
		val job = genTestJob(ScanJobStatus.Completed)

		// add some text
		job.text.set(Full("Text"))
		job.save

		val jobId = job.id.value.get
		val doc = new Document
		val result = doc.documentInfo(
			<lift:Document.showDocument>Document id: <document:id /><br/>Status: <document:status /><br/>Text:<br/><document:text /></lift:Document.showDocument>,
			Full(jobId)
		)

		result.toString should equal(<lift:Document.showDocument>Document id: {jobId}<br/>Status: Completed<br/>Text:<br/>Text</lift:Document.showDocument>.toString)
		
		// delete the scanjob
		job.delete_!
	}
}