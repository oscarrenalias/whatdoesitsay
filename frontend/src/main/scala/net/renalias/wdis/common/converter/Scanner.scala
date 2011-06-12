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
		info("Scanning file: " + file)

		val outFile = file + ".txt"

		// we don't use outFile here because tesseract appends .txt automatically
		val scan = Config.getString_!("tools.tesseract") + " " + file + " " + file + " -l " + lang
		val result = try {
			info("Tesseract processing file: " + file)
			info("command: " + scan)

			val pb = Process(scan)
			pb ! match {
				case 0 => Full(text(outFile))
				case _ => {
					info("There was an error executing Tesseract")
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

		info("Contents of scanned file: " + file + " ==>" + contents)

		contents
	}
}

object Scanner extends Function2[String, String, Box[String]] with TesseractScanner {
	this: AbstractScanner =>
	def apply(file:String, lang:String) = {
		scan(file, lang)
	}
}