package net.renalias.frontend.helpers

import dispatch._
import net.liftweb.json.{Formats, JsonParser}
import net.liftweb.common.{Failure, Box}

object RestHelper {

    /**
   * Executes REST calls to the given URL, and returns the response
   * mapped to a case class object mapped in a Lift Box object, or
   * a Failure object if there was an error performing the call
   *
   * RestHelper.doCall[Response]("http://...")
   *
   * Dispatch's NIO executor is used behind the scenes so that we don't
   * get a thread blocked, but please note that the call to this method
   * *is* blocking
   */
  def doCall[T](u:String)(implicit formats:Formats, mf:Manifest[T]): Box[T] = {
    try {
      val http = new nio.Http
      http(url(u) >- JsonParser.parse)().extractOpt[T]
    } catch {
      case ex:Exception => Failure(ex.toString)
    }
  }

  /**
   * Executes the call and returns a non-blocking dispatch.futures.StoppableFuture
   */
  def doCallAsync(u:String)(implicit formats:Formats) = {
    val http = new nio.Http
    http(url(u) >- JsonParser.parse)
  }
}