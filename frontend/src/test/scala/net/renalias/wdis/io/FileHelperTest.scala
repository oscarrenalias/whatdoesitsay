import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
 
class FileHelperTestSuite extends FunSuite with ShouldMatchers {
	
	import net.renalias.wdis.io.FileHelper
	
	test("getExtension returns the correct extension") {
		FileHelper.getExtension("file.extension").get should equal ("extension")
	}
	
	test("getExtension returns None if the file has no extension") {
		FileHelper.getExtension("noextension") should equal (None)
	}
	
	test("getExtension returns None if the file has no extension but a dot at the end of the name") {
		FileHelper.getExtension("noextension.") should equal (None)
	}	
}