package net.crystopia.onlyup

import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime

class TimeParser {

    fun parseDuration(input: String): Duration {
        val instant = Instant.parse(input)
        val now = OffsetDateTime.now()

        return Duration.between(instant, now)
    }

}