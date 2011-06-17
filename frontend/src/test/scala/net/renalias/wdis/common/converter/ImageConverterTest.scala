import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

import java.io.File

import net.liftweb.common.{Box, Failure, Full, Empty}

import net.renalias.wdis.common.config.Config
import net.renalias.wdis.common.converter._
 
class ImageConverterTest extends FunSuite with ShouldMatchers {
	
	val testFilePath = "./src/test/resources/"
	lazy val testFile1 = (testFilePath + "testfile1.jpg", testFilePath + "testfile1.tiff" )
	lazy val testFile2 = (testFilePath + "testfile2.png", testFilePath + "testfile2.tiff" )
	
	private def deleteIfExists(file:String) = {
		val f = new File(file)
		if(f.exists())
			f.delete()		
	}
	
	test("Images can be converted from JPEG to TIFF correctly") {
		
		// delete the test file in case it already exists
		deleteIfExists(testFile1._2)
		
		// convert our test file and check the output 
		ImageConverter(testFile1._1, testFile1._2) match {
			case Full(file) => {
				file should equal (testFile1._2)
				deleteIfExists(testFile1._2)
			}
			case _ => fail
		} 
	}
	
	test("Images can be converted from PNG to TIFF correctly") {
		
		// delete the test file in case it already exists
		deleteIfExists(testFile2._2)
		
		// convert our test file and check the output 
		ImageConverter(testFile2._1, testFile2._2) match {
			case Full(file) => {
				file should equal (testFile2._2)
				deleteIfExists(testFile2._2)
			}
			case _ => fail
		} 
	}

	test("The system is able to tell which image files need conversion and which don't") {
		val checker = new Object with ImageFileChecker
		checker.isConversionNeeded("whatever.jpg") should be(true)
		checker.isConversionNeeded("whatever.JpeG") should be(true)
		checker.isConversionNeeded("whatever.png") should be(true)
		checker.isConversionNeeded("whatever.PNg") should be(true)
		checker.isConversionNeeded("whatever.tif") should be(false)
		checker.isConversionNeeded("whatever.TifF") should be(false)
	}

	test("The system is able to tell if an image format is supported or not") {
		val checker = new Object with ImageFileChecker
		checker.isSupported("whatever.jpg") should be(true)
		checker.isSupported("whatever.jpeG") should be(true)
		checker.isSupported("whatever.tif") should be(true)
		checker.isSupported("whatever.zxc") should be(false)
	}
}