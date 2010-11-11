package net.renalias.wdis.common.converter

import net.renalias.wdis.common.logger.SimpleLogger
import net.renalias.wdis.common.config.Config
import net.renalias.wdis.common.io.FileHelper._

import xsbt.Process
import java.io.File
import net.liftweb.common._

trait AbstractScanner {
	def scan(file: String, lang: String): Box[String]
}

trait TesseractScanner extends AbstractScanner with Logger {
	override def scan(file: String, lang: String) = {
		println("Scanning file: " + file)

		val outFile = file + ".txt"

		// we don't use outFile here because tesseract appends .txt automatically
		val scan = Config.getString_!("tools.tesseract") + " " + file + " " + file + " -l " + lang
		val result = try {
			println("Tesseract processing file: " + file)
			println("command: " + scan)

			val pb = Process(scan)
			pb ! match {
				case 0 => Full(text(outFile))
				case _ => {
					println("There was an error executing Tesseract")
					Failure("Execution unsuccesful", Full(new Exception("Execution unsuccessful")), Empty)
				}
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

		println("contents of scanned file: " + file + " ==>" + contents)

		contents
	}
}

object Scanner {
	def apply(file:String, lang:String) = {
		val scanner = new Object with TesseractScanner
		scanner.scan(file, lang)
	}
}