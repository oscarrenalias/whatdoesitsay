package net.renalias.ocrservice

import cc.spray._
import marshalling.SprayJsonMarshalling
import net.renalias.ocrservice.OCRTypes._

trait OCRServiceBuilder extends Directives with SprayJsonMarshalling {

	import net.renalias.ocrservice.OCRRequest._

	val scanPipeline: OCRPipelineType
	val sourceFolder: String

	val service = {
		path("scan" / ".*".r) {
			id =>
				get {
					val result = try {
						scanPipeline(OCRRequest(sourceFolder + id, "eng"))
					}  catch {
						case ex:Exception => OCRRequest(inputFile = id, lang = "eng", error = true, errorInfo = Some(ex.toString))
					}
					_.complete(result)
				}
		}
	}
}