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

import java.io.File
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
		
		if (S.get_?) bind("ul", chooseTemplate("choose", "get", xhtml),
	                  	"file_upload" -> fileUpload(ul => theUpload(Full(ul))))
		else {
			
			// save the file to disk if provided
			theUpload.is.map(f => saveFile(f))
			
			bind("ul", chooseTemplate("choose", "post", xhtml),
	          "file_name" -> theUpload.is.map(v => Text(v.fileName)),
	          "mime_type" -> theUpload.is.map(v => Box.legacyNullTest(v.mimeType).map(Text).openOr(Text("No mime type supplied"))), // Text(v.mimeType)),
	          "length" -> theUpload.is.map(v => Text(v.file.length.toString)),
	          "md5" -> theUpload.is.map(v => Text(hexEncode(md5(v.file)))))
		}
	}
}