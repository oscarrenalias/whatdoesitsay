import java.io.File
import net.liftweb.common.{Failure, Full}
import net.renalias.wdis.common.converter.Scanner

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class ScannerTest extends FunSuite with ShouldMatchers {

	val resourcePath = "./src/test/resources/"
	lazy val testFile1 = resourcePath + "testfile3.tiff"

	test("Calling the Scanner class on a TIFF file should provide its contents as text") {
		// delete the temporary file
		val f = new File(testFile1 + ".txt")
		if (f.exists())
			f.delete

		Scanner(testFile1, "ENG") match {
			case Full(text) => text should equal("Happy New Year 2003!\n\n")
			case Failure(msg, _, _) => println("Error was:" + msg); fail
			case _ => fail
		}
	}

	test("If a file could not be scan, Failure should be returned") {
		val contents = Scanner("whatever", "ENG")
		contents should be ('empty)
	}
}