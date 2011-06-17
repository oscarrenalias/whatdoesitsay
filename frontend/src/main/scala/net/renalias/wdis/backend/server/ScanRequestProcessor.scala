package net.renalias.wdis.backend.server

import net.renalias.wdis.common.converter._
import net.renalias.wdis.frontend.model.ScanJob
import net.liftweb.common._
import net.renalias.wdis.common.io.FileHelper
import net.renalias.wdis.common.config.Config

class ScanRequestProcessor(val job:ScanJob) extends Logger with ImageFileChecker {
	
	// does the given file need conversion?
	lazy val convert = isConversionNeeded(job.originalFileName.value)
	var fileWithPath = job.internalFilePath
	val lang = job.lang.toString
	
	def process:Box[String] = {
		info("Processing scan request: file = " + fileWithPath + ", lang = " + lang)
		
		val file = {
			if(convert) {
				val toFile = Config.getString_!("folders.processing") + FileHelper.getFileName(job.internalFileName.value) + ".tiff"
				info("Converting to file:" + toFile)
				ImageConverter(fileWithPath, toFile) match {
					case Full(newFile) => newFile
					case f:Failure => return f
					case _ => return Failure("Could not convert image", Empty, Empty)
				}
			}
			else job.internalFilePath
		}

		info("Scanning file: " + file)

		// execute the scanner
		Scanner(file, lang)		
	}
}