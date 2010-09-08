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

import java.io.File
import _root_.java.util.Date
import net.renalias.wdis.logger.SimpleLogger
import net.renalias.wdis.io._
import net.renalias.wdis.io.FileHelper._
import net.renalias.wdis.model._
import net.renalias.wdis.config._

class Upload extends SimpleLogger {

	// the request-local variable that hold the file parameter
	private object theUpload extends RequestVar[Box[FileParamHolder]](Empty)

	/**
	 * Bind the appropriate XHTML to the form
	 */
	def upload(xhtml: Group): NodeSeq = {
		
		def saveFile(f: FileParamHolder) = {
			
			// svae the file to disk	
			val fileName = FileHelper.randomName
			f.file >>: new File(Config.getString("folders.incoming", "/tmp/incoming") + fileName)
			
			log.debug("Saving file " + f.fileName + " to " + fileName)
			
			// and create a new scanner job in the db
			var job = ScanJob.create.
				originalFileName(f.fileName).
				internalFileName(fileName).
				status(ScanJobStatus.New).
				lang(ScanJobLang.ENG).
				save			
		}
		
		def isImage(f: FileParamHolder): Boolean = {
			FileHelper.getExtension(f.fileName) match {
				case None => false
				case Some(x) if x=="png" || x=="jpg" || x=="jpeg" || x=="tif" || x=="tiff" => true
				case _ => false
			}
		}
		
		if (S.get_?) bind("ul", chooseTemplate("choose", "get", xhtml),
	                  	"file_upload" -> fileUpload(ul => theUpload(Full(ul))))
		else {
			
			if(isImage(theUpload.is.get)) {
			
				// save the file to disk if provided
				theUpload.is.map(f => saveFile(f))
			
				bind("ul", chooseTemplate("choose", "post", xhtml),
		          "file_name" -> theUpload.is.map(v => Text(v.fileName)),
		          "mime_type" -> theUpload.is.map(v => Box.legacyNullTest(v.mimeType).map(Text).openOr(Text("No mime type supplied"))), // Text(v.mimeType)),
		          "length" -> theUpload.is.map(v => Text(v.file.length.toString)),
		          "md5" -> theUpload.is.map(v => Text(hexEncode(md5(v.file)))))
			}
			else {
				S.error("The file was not an image")
				bind("ul", chooseTemplate("choose", "get", xhtml), "file_upload" -> fileUpload(ul => theUpload(Full(ul))))				
			}
		}
	}
}

object UploadWizard extends Wizard with SimpleLogger {
	  
	object completeInfo extends WizardVar(false)
	
	def isImage(f: FileParamHolder): Boolean = {
		FileHelper.getExtension(f.fileName) match {
			case None => false
			case Some(x) if x=="png" || x=="jpg" || x=="jpeg" || x=="tif" || x=="tiff" => true
			case _ => false
		}
	}	

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
				
			override def validations = List(f=> f match {
				case Full(f) if isImage(f) => Nil
				case Full(f) if !isImage(f) => List(FieldError(currentField.box openOr new FieldIdentifier{}, Text(S ?? "The file you have provided is not an image")))
				case _ => List(FieldError(currentField.box openOr new FieldIdentifier{}, Text(S ?? "You must provide a document to scan")))
			})
		}

	    // choose the next screen based on the age
	    override def nextScreen = confirmationScreen
  	}

	val confirmationScreen = new Screen {
		
	}
	
  	// what to do on completion of the wizard
  	def finish() {
		
		def saveFile(f: FileParamHolder, lang:String) = {
			
			// svae the file to disk	
			val fileName = FileHelper.randomName + "." + (FileHelper.getExtension(f.fileName) getOrElse "")
			f.file >>: new File(Config.getString("folders.incoming", "/tmp/whatdoesitsay/incoming") + fileName)			
			log.debug("Saving file " + f.fileName + " to " + fileName)
			
			// and a copy to the static file folder
			//val imagesFolder:String = Box(Config.getString("folders.images")) open_!	// this parameter must be configured, throw exeption otherwise 
			f.file >>: new File(Config.getString("folders.images", "/tmp/whatdoesitsay/images") + fileName)
			log.debug("Saving original image to static image folder")			
			
			// and create a new scanner job in the db
			var job = ScanJob.create.
				originalFileName(f.fileName).
				internalFileName(fileName).
				status(ScanJobStatus.New).
				lang(ScanJobLang.ENG).
				save			
		}
	
		fileAndLanguageSelection.file.is match {
			case Full(f) => { 
				saveFile(f, "ENG")
				S.notice("File saved successfully!")
			}
			case _ => S.error("nothing was uploaded mate!")
		}
    	completeInfo.set(true)

		// redirect processing to the page where we wait for the processing to be completed
		S.seeOther("/document")
  	}
}