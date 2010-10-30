package net.renalias.wdis.common.converter

import net.liftweb.common.{Box, Failure, Full, Empty}

import net.renalias.wdis.common.logger.SimpleLogger
import net.renalias.wdis.common.config.Config
import net.renalias.wdis.common.io.FileHelper._

import xsbt.Process
import java.io.File

trait AbstractScanner {
	def scan(file: String, lang: String): Box[String]
}

trait TesseractScanner extends AbstractScanner with SimpleLogger {
	override def scan(file: String, lang: String) = {
		log.debug("Scanning file: " + file)

		val outFile = file + ".txt"

		// we don't use outFile here because tesseract appends .txt automatically
		val scan = Config.getString("tools.tesseract", "") + " " + file + " " + file + " -l " + lang
		val result = try {
			log.debug("Tesseract processing file: " + file)
			log.debug("command: " + scan)

			val pb = Process(scan)
			pb ! match {
				case 0 => Full(text(outFile))
				case _ => Failure("Execution unsuccesful", Full(new Exception("Execution unsuccessful")), Empty)
			}
		} catch {			
			case ex:Exception => Failure(ex.toString, Full(ex), Empty)
		}

		// delete the output file if it exists
		val f = new File(outFile)
		if(f.exists)
			f.delete

		result
	}
	
	private def text(file:String) = {
		val f = new File(file)
		val contents = f.read

		log.debug("contents of scanned file: " + file + " ==>" + contents)

		contents
	}
}

object Scanner {
	def apply(file:String, lang:String) = {
		val scanner = new Object with TesseractScanner
		scanner.scan(file, lang)
	}
}