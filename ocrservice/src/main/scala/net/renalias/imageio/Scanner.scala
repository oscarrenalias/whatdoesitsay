package net.renalias.imageio

import xsbt.Process
import java.io.File
import akka.util.Logging
import net.renalias.config.Config
import net.renalias.imageio.FileHelper._
import net.renalias.imageio.ScannerTypes._
import scala.{Right, Some}

object ScannerTypes {
	type ScannerResultType = Either[Option[Exception],String]

	trait AbstractScanner {
		def scan(file: String, lang: String): ScannerResultType
	}
}

trait TesseractScanner extends AbstractScanner with Logging {
	override def scan(file: String, lang: String) = {
		log.info("Scanning file: " + file)

		val outFile = file + ".txt"

		// we don't use outFile here because tesseract appends .txt automatically
		val scan = Config.getString_!("tools.tesseract") + " " + file + " " + file + " -l " + lang
		val result = try {
			log.info("Tesseract processing file: " + file)
			log.info("command: " + scan)

			val pb = Process(scan)
			pb ! match {
				case 0 => Right(text(outFile))
				case _ => {
					log.info("There was an error executing Tesseract")
					Left(Some(new Exception("There was an error scanning the given image")))
				}
			}
		} catch {			
			case ex:Exception => Left(Some(ex))
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

		log.info("Contents of scanned file: " + file + " ==>" + contents)

		contents
	}
}

object Scanner extends Function2[String, String, ScannerResultType] with TesseractScanner {
	this: AbstractScanner =>
	def apply(file:String, lang:String) = scan(file, lang)
}

// mock to be used for testing
object ScannerTest extends Function2[String, String, ScannerResultType] {
	def apply(file:String, lang:String) = Right("These are the OCR results")
}