package net.renalias.imageio

import java.io._
import net.renalias.helpers.StringHelpers

class FileHelper(val file : File) {
	// saves data to the given file in binary format
  	def >>: (data: Array[Byte]): Unit = {
		val fos = new FileOutputStream(file)
		fos.write(data)
	}
	
	def read(encoding:String = "UTF-8") = scala.io.Source.fromFile(file, encoding).mkString

	def read = scala.io.Source.fromFile(file).mkString
}

object FileHelper {
  	implicit def file2helper(file : File) = new FileHelper(file)

	def randomName() = StringHelpers.randomString
	
	def isImage: Boolean = true

  def isReadable(f:String) = (new File(f)).canRead
	
	def getExtension(fileName:String): Option[String] = {
		fileName.lastIndexOf('.') match {
			case -1 => None
			case x:Int if x==(fileName.length-1) => None
			case x:Int => new Some(fileName.substring(x+1).toLowerCase)
		}		
	}

	def getFileName(fileName:String): String = {
		fileName.lastIndexOf('.') match {
			case -1 => fileName
			case x:Int => fileName.substring(0, x)
		}
	}

	// helps in pattern matching
	object FileExtension {
		def apply(f:String) = getExtension(f)
		def unapply(f:String) = apply(f)
	}

	object FileName {
		def apply(f:String) = getFileName(f)
		def unapply(f:String) = apply(f)
	}
}