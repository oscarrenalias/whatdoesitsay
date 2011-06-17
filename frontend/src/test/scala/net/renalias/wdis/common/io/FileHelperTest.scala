import java.io.File
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
 
class FileHelperTestSuite extends FunSuite with ShouldMatchers {
	
	import net.renalias.wdis.common.io.FileHelper
	import net.renalias.wdis.common.io.FileHelper._
	
	test("getExtension returns the correct extension") {
		FileHelper.getExtension("file.extension").get should equal ("extension")
	}
	
	test("getExtension returns None if the file has no extension") {
		FileHelper.getExtension("noextension") should equal (None)
	}
	
	test("getExtension returns None if the file has no extension but a dot at the end of the name") {
		FileHelper.getExtension("noextension.") should equal (None)
	}

	test("Reading files works") {
		val f = new File("./src/test/resources/testfile.txt")
		f.read("UTF-8") should equal ("file contents")
	}

	test("File extensions are returned correctly") {
		FileHelper.getFileName("file.ext") should equal("file")
	}

	test("For files with multiple . in the filename, the extension is correctly returned") {
		FileHelper.getFileName("file.ext1.ext2") should equal("file.ext1")
	}

	test("For files with no extension, the entire name is returned") {
		FileHelper.getFileName("file") should equal("file")		
	}
}