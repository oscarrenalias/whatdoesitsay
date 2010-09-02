package net.renalias.wdis.io

import net.liftweb.util.StringHelpers
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

	def randomName() = StringHelpers.randomString(32)
	
	def isImage: Boolean = true
}