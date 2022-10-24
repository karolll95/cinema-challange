package com.radbrackets.cinema

import java.time.LocalTime

object WorkingHours {
    val OPENED_FROM: LocalTime = LocalTime.of(8, 0)
    val OPENED_TO: LocalTime = LocalTime.of(22, 0)
    val PREMIERE_FROM: LocalTime = LocalTime.of(17, 0)
    val PREMIERE_TO: LocalTime = LocalTime.of(21, 0)
}