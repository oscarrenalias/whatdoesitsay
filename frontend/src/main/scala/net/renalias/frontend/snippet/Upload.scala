package net.renalias.frontend.snippet

import _root_.net.liftweb._
import http._
import S._
import util._
import _root_.scala.xml.{NodeSeq, Text, Group}
import _root_.net.liftweb.http._
import _root_.net.liftweb.common._

import java.io.File

import net.renalias.frontend.model._
import net.renalias.frontend.config.PimpedProps
import net.renalias.frontend.helpers.FileHelper._
import wizard.Wizard
import net.renalias.frontend.helpers.FileHelper
import net.renalias.frontend.comet._

trait UploadWizardTrait extends Wizard with Logger {
	def isImage(f: FileParamHolder): Boolean = {
		f.fileName match {
			case FileExtension(x) if x == "png" || x == "jpg" || x == "jpeg" || x == "tif" || x == "tiff" => true
			case _ => false
		}
	}

	lazy val imageFolder = PimpedProps.get_!("folders.static")

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
				SHtml.select(langValues, Box(Some("EN")), v => set(v))
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
    // TODO: show the uploaded image here and get the user to confirm that this is the one
	}

	// what to do on completion of the wizard
	def finish() {

		def saveFile(f: FileParamHolder, jobId: String, lang: String) = {

			// save the file to disk	
			val fileName = jobId + "." + (FileExtension(f.fileName) getOrElse "")

			f.file >>: new File(PimpedProps.getf("files.incoming", { _ + fileName }))
			debug("Saving file " + f.fileName + " to " + fileName)

			// and a copy to the static file folder
			f.file >>: new File(PimpedProps.getf("files.static", { _ + fileName }))
			debug("Saving original image to static image folder")

			// and create a new scanning job in the db
			var result = ScanJob.createRecord
			  .originalFileName(f.fileName)
			  .internalFileName(fileName)
			  .status(ScanJobStatus.New)
			  .lang(ScanJobLang.ENG)
        .saveTheRecord  // saveTheRecord returns a Box, which is good

      // notify the comet actor if it went well
      result map { job =>
        debug("ScanJob created succesfully, notifying Comet actor...")
        //val actor = new ScanJobActor
        //actor ! NewScanRequest(job.id.is.toString, job.internalFileName.get)
        S.session map { sess =>
          debug("Sending CometActor a message to perform the REST request...")
          sess.sendCometActorMessage("ScanJobActor", Full(job.id.is.toString), NewScanRequest(job.id.is.toString, job.internalFileName.get))
        }

        job // return the unmodified value
      }
      // Box.map returns the box unmodified, so no need to specifically return anything else
		}

		fileAndLanguageSelection.file.get match {
			case Full(f) => {
				val fileId = FileHelper.randomName
        // TODO: fix the language here
				saveFile(f, fileId, "ENG") match {
					case Full(job) => S.seeOther("/document/" + job.id.is.toString)
					case _ => S.error("There was an error processing your request"); error("Error saving job for file: " + f)
				}
			}
			case _ => S.error("No file was uploaded")
		}
	}
}

// get the name in lower case so that it can be used as a snippet
object uploadwizard extends UploadWizardTrait