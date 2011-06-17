package net.renalias

import config.Config
import config.Config.ConfigKeyNotFoundException
import org.specs.Specification

class ConfigSpec extends Specification {

	"Config" should {
		"Return None when a non-existant key is requested" in {
			Config.getString("non.existant") must beNone
		}
		"Return the value wrapped in a Some if found" in {
			Config.getString("tools.convert") must beSomething
		}
		"Throw an exception if the key is not found when using getString_!" in {
			Config.getString_!("non.existant") must throwA[ConfigKeyNotFoundException]
		}
	}
}