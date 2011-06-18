package net.renalias.ocrservice

import akka.event.slf4j.Logging
import net.renalias.ocrservice.OCRTypes.OCRServicePipelineComponent
import net.renalias.imageio.{FileHelper, Scanner, ImageConverter, ImageFileChecker}
import cc.spray.json.JsObject._
import cc.spray.json.JsBoolean._
import cc.spray.json.JsString._
import cc.spray.json._
import scala.Some
import net.renalias.config.Config

object OCRTypes {
	type OCRPipelineType = (OCRRequest) => (OCRRequest)
	trait OCRServicePipelineComponent extends Function1[OCRRequest, OCRRequest]
}

object ImageConversionService extends PartialFunction[OCRRequest,OCRRequest] {
	def apply(info:OCRRequest) = {
		// is conversion needed?
		ImageFileChecker.isConversionNeeded(info.inputFile) match {
			case true => ImageConverter.toFormat(info.inputFile, "tiff").fold({ ex => throw ex.get }, { f => info.outputFile = Some(f) })
			case false => info.outputFile = Some(info.inputFile)
		}
		info
	}

	def isDefinedAt(r:OCRRequest) = {
		ImageFileChecker.isSupported(r.inputFile)
	}
}

object OCRService extends OCRServicePipelineComponent {
	def apply(info:OCRRequest) = {
		Scanner(info.outputFile.get, info.lang).fold({ ex => throw ex.get }, { text => info.result = Some(text); info })
	}
}

object OCRServiceLogger extends OCRServicePipelineComponent with Logging {
	def apply(info: OCRRequest) = {
		log.info("Processing OCR request = " + info)
		// return the object unchanged
		info
	}
}

object FileArchiver extends OCRServicePipelineComponent with Logging {
	def apply(info: OCRRequest) = {
		log.info("Archiving file: " + info.outputFile + " (TODO)")
		info
	}
}

sealed case class OCRRequest(inputFile:String, lang:String, var outputFile: Option[String] = None,
                             var result:Option[String] = None, error:Boolean = false, errorInfo:Option[String] = None)

object OCRRequest extends DefaultJsonProtocol {
	implicit object OCRRequestJsonFormat extends JsonFormat[OCRRequest] {
		def write(r:OCRRequest) = {
			JsObject(
				JsField("input", JsString(r.inputFile)),
				JsField("text", JsString(r.result.getOrElse(""))),
				JsField("lang", JsString(r.lang)),
				JsField("error", JsBoolean(r.error)),
				JsField("errorInfo", JsString(r.errorInfo.getOrElse("")))
			)
		}

		def read(v: JsValue) = v match {
			case JsObject(List(
					JsField("input", JsString(inputFile)),
					JsField("text", JsString(text)),
					JsField("lang", JsString(lang)),
					JsField("error", JsBoolean(error)),
					JsField("errorInfo", JsString(errorInfo)))
				) => {
				new OCRRequest(inputFile, lang, None, Some(text), error, Some(errorInfo))
			}
			case _ => throw new DeserializationException("Error unmarshalling object")
		}
	}
}

trait ConvertAndScan {
	lazy val sourceFolder = Config.getString_!("folders.incoming")
	lazy val scanPipeline = OCRServiceLogger andThen ImageConversionService andThen OCRService andThen FileArchiver
}

trait ConvertAndScanTest {
	lazy val sourceFolder = ""
	lazy val scanPipeline:PartialFunction[OCRRequest, OCRRequest] = {
		// if "test.fail" is given as the file name, that will trigger a failure response
		case OCRRequest("test.fail", _, _, _, _, _) => OCRRequest("test.fail", "eng", None, None, true, Some("This is an error message"))
		case r:OCRRequest => { r.result = Some("Test result"); r }
	}
}