package net.renalias.frontend.model

import net.renalias.frontend.config.PimpedProps
import net.renalias.frontend.helpers.RestHelper
import net.liftweb.common.Box

case class OCRServiceResponse(input:String, text:Option[String], lang:String, error:Boolean, errorInfo:Option[String])

object OCRServiceRequest {
  def call(file:String) = {
    implicit val formats = net.liftweb.json.DefaultFormats
    RestHelper.doCall[OCRServiceResponse](PimpedProps.getf("ocrservice.url", { _ + file }))
  }
}