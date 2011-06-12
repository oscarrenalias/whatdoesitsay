package net.renalias.wdis.frontend.snippet

import _root_.net.liftweb._
import http._
import S._
import util._
import _root_.scala.xml.{NodeSeq, Text, Group}
import _root_.net.liftweb.http._
import _root_.net.liftweb.wizard._
import _root_.net.liftweb.common._

import java.io.File

import net.renalias.wdis.common.io._
import net.renalias.wdis.common.io.FileHelper._
import net.renalias.wdis.frontend.model._
import net.renalias.wdis.common.messaging._
import net.renalias.wdis.common.config.{ComponentRegistry, Config}

object UploadWizard extends Wizard with Logger {
	def isImage(f: FileParamHolder): Boolean = {
		FileHelper.getExtension(f.fileName) match {
			case None => false
			case Some(x) if x == "png" || x == "jpg" || x == "jpeg" || x == "tif" || x == "tiff" => true
			case _ => false
		}
	}

	lazy val incomingFolder = Config.getString_!("folders.incoming")	
	lazy val imageFolder = Config.getString_!("folders.static")

	// define the first screen
	val fileAndLanguageSelection = new Screen {
		override val onConfirm_? = true

		// language
		val lang = new Field {
			def name = "Language"

			type ValueType = String

			def default = "EN"

			lazy val manifest = buildIt[ValueType]

			// list of languages			
			lazy val langValues = List(("ENG", S ?? "English"), ("DEU", S ?? "German"), ("ESP", S ?? "Spanish"))

			override def toForm = {
				SHtml.select(langValues, Box("EN"), v => set(v))
			}
		}

		val file = new Field {
			override def uploadField_? = true

			def name = "Document"

			def default = Empty

			type ValueType = Box[FileParamHolder]
			lazy val manifest = buildIt[Box[FileParamHolder]]

			// If field is empty, show upload dialog, else show name 
			override def toForm = SHtml.fileUpload(fu => set(Full(fu))) /*is match {
					case Full(f) => <span>{f.name}</span>
					case _ => <span>{SHtml.fileUpload(fu => set(Full(fu)))}</span>					
				}*/

			override def validations = List(f => f match {
				case Full(f) if isImage(f) => Nil
				case Full(f) if !isImage(f) => List(FieldError(currentField.box openOr new FieldIdentifier {}, Text(S ?? "The file you have provided is not an image")))
				case _ => List(FieldError(currentField.box openOr new FieldIdentifier {}, Text(S ?? "You must provide a document to scan")))
			})
		}

		// choose the next screen based on the age
		override def nextScreen = confirmationScreen
	}

	val confirmationScreen = new Screen {
	}

	// what to do on completion of the wizard
	def finish() {

		def saveFile(f: FileParamHolder, jobId: String, lang: String) = {

			// save the file to disk	
			val fileName = jobId + "." + (FileHelper.getExtension(f.fileName) getOrElse "")

			f.file >>: new File(incomingFolder + fileName)
			debug("Saving file " + f.fileName + " to " + fileName)

			// and a copy to the static file folder
			//val imagesFolder:String = Box(Config.getString("folders.images")) open_!	// this parameter must be configured, throw exeption otherwise 
			f.file >>: new File(imageFolder + fileName)
			debug("Saving original image to static image folder")

			// and create a new scanning job in the db
			var job = ScanJob.createRecord
			job.originalFileName.set(f.fileName)
			job.internalFileName.set(fileName)
			job.status.set(ScanJobStatus.New)
			job.lang.set(ScanJobLang.ENG)
			val result = job.save

			result.map({job => ComponentRegistry.backendServer ! NewAsyncScanRequest(job.id.value.get)})

			result
		}

		fileAndLanguageSelection.file.is match {
			case Full(f) => {
				val fileId = FileHelper.randomName
				saveFile(f, fileId, "ENG") match {
					case Full(job) => S.seeOther("/document/" + job.id.value.get)
					case _ => S.error("There was an error processing your request"); error("Error saving job for file: " + f)
				}
			}
			case _ => S.error("No file was uploaded")
		}
	}
}