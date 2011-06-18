package net.renalias.imageio

import xsbt.Process
import akka.event.slf4j.Logging
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

		val convert = Config.getString_!("tools.convert") + " " + file + " " + toFile
		val result = try {
			log.debug("ImageMagick converting file: " + file)
			log.debug("command: " + convert)
			
			val pb = Process(convert)
			pb ! match {
				case 0 => Right(toFile)
				case _ => Left(Some(new Exception("There was an error converting the image file")))
			}
		} catch {			
			case ex:Exception => Left(Some(ex))
		}
		
		result
	}
}

trait GraphicsMagickConverter extends AbstractConverter with Logging {

	override def convert(file: String, toFile: String) = {

		val convert = Config.getString_!("tools.gm_command").replace("{1}", file).replace("{2}", toFile)
		val result = try {
			log.debug("ConvertMagick converting file: " + file)
			log.debug("command: " + convert)

			val pb = Process(convert)
			pb ! match {
				case 0 => Right(toFile)
				case _ => Left(Some(new Exception("There was an error converting the image file")))
			}
		} catch {
			case ex:Exception => Left(Some(ex))
		}

		result
	}
}

object ImageConverter extends GraphicsMagickConverter with Function2[String, String, ConverterResultType] {
	this: AbstractConverter =>

	def apply(file: String, toFile: String) = convert(file, toFile)
	def toFormat(file:String, targetFormat: String) = convert(file, file + "." + targetFormat)
}

object ImageFileChecker {

	// list of formats that need no conversion
	private val NO_CONVERSION = List( "tif", "tiff" )
	// list of accepted formats
	private val SUPPORTED_FORMATS = List("jpg", "jpeg", "tiff", "tif", "png" )

	/**
	 * Checks if the given image file requires conversion
	 */
	val isConversionNeeded = (f:String) => FileHelper.getExtension(f).map({ext => !NO_CONVERSION.contains(ext.toLowerCase)}).getOrElse(true)

	/**
	 * Checks if the image format is supported
	 */
	val isSupported = (f:String) => FileHelper.getExtension(f).map({ext => SUPPORTED_FORMATS.contains(ext.toLowerCase)}).getOrElse(false)
}