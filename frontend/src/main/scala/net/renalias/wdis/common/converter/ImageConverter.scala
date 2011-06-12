package net.renalias.wdis.common.converter

import net.renalias.wdis.common.config.Config
import net.renalias.wdis.common.io.FileHelper

import xsbt.Process
import net.liftweb.common._

trait AbstractConverter {
	def convert(file: String, toFile: String): Box[String]
}

trait ImageMagickConverter extends AbstractConverter with Logger {
	override def convert(file: String, toFile: String) = {
		
		// FIXME
		val convert = Config.getString_!("tools.convert") + " " + file + " " + toFile
		val result = try {
			debug("ImageMagick converting file: " + file)
			debug("command: " + convert)
			
			val pb = Process(convert)
			pb ! match {
				case 0 => Full(toFile)
				case _ => Failure("Execution unsuccesful", Full(new Exception("Execution unsuccessful")), Empty)
			}
		} catch {			
			case ex:Exception => Failure(ex.toString, Full(ex), Empty)
		}
		
		result
	}
}

object ImageConverter extends ImageMagickConverter with Function2[String, String, Box[String]] {
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