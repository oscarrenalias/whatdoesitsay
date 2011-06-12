package net.renalias.imageio

import xsbt.Process
import akka.util.Logging
import net.renalias.config.Config
import net.renalias.imageio.ConverterTypes._

object ConverterTypes {
	type ConverterResultType = Either[Option[Exception],String]
	trait AbstractConverter {
		def convert(file: String, toFile: String): ConverterResultType
	}
}

trait ImageMagickConverter extends AbstractConverter with Logging {
	override def convert(file: String, toFile: String) = {
		
		// FIXME
		val convert = Config.getString_!("tools.convert") + " " + file + " " + toFile
		val result = try {
		log.debug("ImageMagick converting file: " + file)
		log.debug("command: " + convert)
			
			val pb = Process(convert)
			pb ! match {
				case 0 => Right(toFile)
				case _ => Left(Some(new Exception("Execution unsuccessful")))
			}
		} catch {			
			case ex:Exception => Left(Some(ex))
		}
		
		result
	}
}

object ImageConverter extends ImageMagickConverter with Function2[String, String, ConverterResultType] {
	this: AbstractConverter =>
	def apply(file: String, toFile: String) = {
		convert(file, toFile)
	}
}

trait ImageFileChecker {

	// list of formats that need no conversion
	private val NO_CONVERSION = List( "tif", "tiff" )
	// list of accepted formats
	private val SUPPORTED_FORMATS = List("jpg", "jpeg", "tiff", "tif", "png" )

	/**
	 * Checks if the given image file requires conversion
	 */
	def isConversionNeeded(file: String) = {
		FileHelper.getExtension(file) match {
			case Some(ext) => !NO_CONVERSION.contains(ext.toLowerCase)
			case None => true
		}
	}

	/**
	 * Checks if the image format is supported
	 */
	def isSupported(file: String) = {
		FileHelper.getExtension(file) match {
			case Some(ext) => SUPPORTED_FORMATS.contains(ext.toLowerCase)
			case None => false
		}
	}
}