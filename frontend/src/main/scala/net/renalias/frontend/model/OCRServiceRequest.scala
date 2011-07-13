package net.renalias.frontend.model

import net.renalias.frontend.helpers.RestHelper
import actors.Actor
import net.liftweb.common.Logger._
import net.liftweb.common.{Logger, Full, Box}
import net.renalias.frontend.comet.{ScanRequestReady, UpdateScanRequest, NewScanRequest}
import net.liftweb.http.S
import net.renalias.frontend.config.Config

case class OCRServiceResponse(input: String, text: Option[String], lang: String, error: Boolean, errorInfo: Option[String])

object OCRServiceRequest {
  def call(file: String) = {
    implicit val formats = net.liftweb.json.DefaultFormats
    RestHelper.doCall[OCRServiceResponse](Config.getStringF("ocrservice.url", {_ + file }))
  }
}