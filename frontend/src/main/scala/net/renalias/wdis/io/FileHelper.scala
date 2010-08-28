package net.renalias.wdis.io

import scala.runtime.RichLong
import java.io._

class FileHelper(file : File) {
	// saves data to the given file in binary format
  	def >>: (data: Array[Byte]): Unit = {
		val fos = new FileOutputStream(file)
		fos.write(data)
	}
}

object FileHelper {
  	implicit def file2helper(file : File) = new FileHelper(file)

	def randomName(): String = System.currentTimeMillis().toHexString
}