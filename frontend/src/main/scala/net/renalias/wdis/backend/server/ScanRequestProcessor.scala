package net.renalias.wdis.backend.server

import net.renalias.wdis.common.logger.SimpleLogger
import net.renalias.wdis.common.config.Config

class ScanRequestProcessor(val file:String, val lang:String) extends SimpleLogger {
	
	object Scanner {
		def scan(file: String): Either[java.lang.Throwable, String] = {
			log.debug("Scanning file: " + file)
			
			Right("this is some hardcoded text")
		}
	}
	
	// does the given file need conversion
	lazy val convert = false
	
	def process = {
		log.info("Processing scan request: file = " + file + ", lang = " + lang)
		
		if(convert) {
			// convert the file first
			log.info("Converting file: " + file)
		}
		
		// execute the scanner
		Scanner.scan(file)		
	}
}