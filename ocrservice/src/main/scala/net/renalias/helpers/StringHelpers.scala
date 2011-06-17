package net.renalias.helpers

import java.util.UUID

object StringHelpers {
	def randomString = UUID.randomUUID().toString();
}