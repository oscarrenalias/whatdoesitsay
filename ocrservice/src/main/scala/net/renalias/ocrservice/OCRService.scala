package net.renalias.ocrservice

import akka.util.Logging
import net.renalias.ocrservice.OCRTypes.OCRServicePipelineComponent
import net.renalias.imageio.{Scanner, ImageConverter, ImageFileChecker}

object OCRTypes {
	type OCRPipelineType = (OCRRequest) => (OCRRequest)
	trait OCRServicePipelineComponent extends Function1[OCRRequest, OCRRequest]
}

object ImageConversionService extends OCRServicePipelineComponent {
	def apply(info:OCRRequest) = {

		// is conversion needed?
		ImageFileChecker.isConversionNeeded(info.inputFile) match {
			case true => ImageConverter.toFormat(info.inputFile, "tiff").fold({ ex => throw ex.get }, { f => info.outputFile = Some(f) })
			case false => info.outputFile = Some(info.inputFile)
		}

		info
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

sealed case class OCRRequest(inputFile:String, lang:String, var outputFile: Option[String] = None, var result:Option[String] = None)

trait ConvertAndScan {
	val scanPipeline = OCRServiceLogger andThen ImageConversionService andThen OCRService
}

trait ConvertAndScanTest {
	val scanPipeline = (f:OCRRequest) => {
		f.result = Some("Test result")
		f
	}
}