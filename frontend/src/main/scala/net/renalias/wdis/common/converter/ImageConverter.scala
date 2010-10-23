package net.renalias.wdis.common.converter

import net.liftweb.common.{Box, Failure, Full, Empty}

import net.renalias.wdis.common.logger.SimpleLogger
import net.renalias.wdis.common.config.Config

import xsbt.Process
import xsbt.Process._

trait AbstractConverter {
	def convert(file: String, toFile: String): Box[String]
}

trait ImageMagickConverter extends AbstractConverter with SimpleLogger {
	override def convert(file: String, toFile: String) = {
		
		// FIXME
		val convert = Config.getString("tools.convert", "") + " " + file + " " + toFile
		val result = try {
			log.debug("ImageMagick converting file: " + file)
			log.debug("command: " + convert)
			
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

object ImageConverter extends ImageMagickConverter {
	def apply(file: String, toFile: String) = {
		val converter = new Object with ImageMagickConverter
		converter.convert(file, toFile)
	}
}