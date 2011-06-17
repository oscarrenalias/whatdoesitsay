package net.renalias

import cc.spray._
import http._
import HttpMethods._
import HttpStatusCodes._
import ocrservice.{ConvertAndScanTest, OCRServiceBuilder}
import org.specs.Specification
import test.SprayTest

class OCRServiceBuilderTest extends Specification with SprayTest with OCRServiceBuilder with ConvertAndScanTest {
  "The OCR Service" should {
    "Return some data for the /scan route" in {
      testService(HttpRequest(GET, "/scan")) {
        service
      }.response.content.as[String] mustEqual Right("Test result")
    }
    "leave GET requests to other paths unhandled" in {
      testService(HttpRequest(GET, "/kermit")) {
        service
      }.handled must beFalse
    }
    "return a MethodNotAllowed error for POST requests to the root path" in {
      testService(HttpRequest(POST, "/")) {
        service
      }.response mustEqual failure(MethodNotAllowed, "HTTP method not allowed, supported methods: GET")
    }
  }
}