package net.renalias.wdis.common.io

import net.liftweb.util.StringHelpers

import java.io._
import java.util.Scanner

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

	def randomName() = StringHelpers.randomString(32)
	
	def isImage: Boolean = true
	
	def getExtension(fileName:String): Option[String] = {
		fileName.lastIndexOf('.') match {
			case -1 => None
			case x:Int if x==(fileName.length-1) => None
			case x:Int => new Some(fileName.substring(x+1).toLowerCase)
		}		
	}
}