package com.radbrackets.cinema.util

import java.time.LocalDateTime

internal data class TimeRange(
    val from: LocalDateTime,
    val to: LocalDateTime
)