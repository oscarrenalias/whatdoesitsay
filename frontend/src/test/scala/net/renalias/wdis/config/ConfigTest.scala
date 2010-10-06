import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
 
class ConfigTestSuite extends FunSuite with ShouldMatchers {
	
	import net.renalias.wdis.config.Config
	
	test("Config can return known values") {
		Config.getString("folders.incoming").get should equal ("../files/incoming/")
	}
	
	test("Config returns default values if key is not found") {
		Config.getString("whatever", "default") should equal ("default")
	}
	
	test("Config returns None for keys that don't exist") {
		Config.getString("whatever") should be (None)
	}
}