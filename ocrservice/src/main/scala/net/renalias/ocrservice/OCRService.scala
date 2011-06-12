package net.renalias.ocrservice

import akka.util.Logging
import net.renalias.ocrservice.OCRTypes.OCRServicePipelineComponent

object OCRTypes {
	type OCRPipelineType = (OCRRequest) => (OCRRequest)
	trait OCRServicePipelineComponent extends Function1[OCRRequest, OCRRequest]
}

object ImageConversionService extends OCRServicePipelineComponent {
	def apply(info:OCRRequest) = {

		// set the new file name
		info.outputFile = Some("outputfile.tiff")

		// return the updated input object
		info
	}
}

object OCRService extends OCRServicePipelineComponent {
	def apply(info:OCRRequest) = {
		info.result = Some("these are the contents of the file")

		// return the updated object
		info
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