package net.renalias.wdis.backend.server

import net.renalias.wdis.common.converter._
import net.renalias.wdis.frontend.model.ScanJob
import net.liftweb.common._

class ScanRequestProcessor(val job:ScanJob) extends Logger with ImageFileChecker {
	
	// does the given file need conversion?
	lazy val convert = isConversionNeeded(job.originalFileName.value)
	var file = job.internalFilePath
	val lang = job.lang.toString
	
	def process:Box[String] = {
		info("Processing scan request: file = " + file + ", lang = " + lang)
		
		if(convert) {
			// convert the file first
			val toFile = "newfile.tiff" // TODO: fix me
			ImageConverter(file, toFile) match {
				case Full(newFile) => file = newFile
				case f:Failure => return f
			}
		}
		
		// execute the scanner
		Scanner(file, lang)		
	}
}