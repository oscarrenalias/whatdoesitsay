package net.renalias.ocrservice

import cc.spray._

trait OCRServiceBuilder extends ServiceBuilder {
  
  val service = {
    path("scan" / ".*".r) { id =>
      get { 
		_.complete("Processing file = " + id) 
	  }
    }
  } 
}