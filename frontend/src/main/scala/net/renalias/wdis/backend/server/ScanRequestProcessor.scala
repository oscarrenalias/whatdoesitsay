package net.renalias.wdis.backend.server

import net.renalias.wdis.common.logger.SimpleLogger
import net.renalias.wdis.common.config.Config
import net.renalias.wdis.common.converter._
import net.liftweb.common.Box

class ScanRequestProcessor(val file:String, val lang:String) extends SimpleLogger {
	
	// does the given file need conversion
	lazy val convert = false
	
	def process:Box[String] = {
		log.info("Processing scan request: file = " + file + ", lang = " + lang)
		
		if(convert) {
			// convert the file first
			log.info("Converting file: " + file)
		}
		
		// execute the scanner
		Scanner(file, lang)		
	}
}