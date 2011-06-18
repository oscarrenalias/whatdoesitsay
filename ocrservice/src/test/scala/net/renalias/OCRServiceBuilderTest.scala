package net.renalias

import ocrservice.{OCRRequest, ConvertAndScanTest, OCRServiceBuilder}
import org.specs.Specification
import cc.spray._
import marshalling.SprayJsonMarshalling
import test._
import http._
import HttpMethods._

class OCRServiceBuilderTest extends Specification with SprayTest with OCRServiceBuilder with ConvertAndScanTest with SprayJsonMarshalling {

	import net.renalias.ocrservice.OCRRequest._

  "The OCR Service" should {
    "Return some data for the /scan route" in {
      testService(HttpRequest(GET, "/scan")) {
        service
      }.response.content.as[OCRRequest] match {
	      case Right(OCRRequest(_, _, _, Some(text), error,  _)) => {
		      text mustEqual "Test result"
		      error mustBe false
	      }
	      case _ => fail("Incorrect response")
      }
    }
	  "Return an error status and an error description for error cases" in {
			testService(HttpRequest(GET, "/scan/test.fail")) {
        service
      }.response.content.as[OCRRequest] match {
	      case Right(OCRRequest(_, _, _, _, error, Some(errorInfo))) => {
		      error mustBe true
		      //errorInfo must beSomething
		      errorInfo mustNot beEmpty
	      }
	      case _ => fail("Incorrect error response")
      }
	  }
    "leave GET requests to other paths unhandled" in {
      testService(HttpRequest(GET, "/kermit")) {
        service
      }.handled must beFalse
    }
  }
}