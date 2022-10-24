package com.radbrackets.cinema.domain

import com.radbrackets.cinema.WorkingHours
import com.radbrackets.cinema.room.availability.RoomAvailabilityChecker
import com.radbrackets.cinema.util.TimeRange
import java.time.LocalTime
import java.util.UUID

internal class RoomEventValidator(
    private val roomAvailabilityChecker: RoomAvailabilityChecker
) {
    fun validateRoomAvailability(
        roomId: UUID,
        timeRange: TimeRange
    ) {
        if (!roomAvailabilityChecker.isAvailable(roomId, timeRange).value) {
            throw IllegalStateException("Room with id=$roomId is unavailable within given time range!")
        }
    }

    fun validateWorkingHours(
        showStartingTime: LocalTime,
        showEndingTime: LocalTime
    ) {
        when {
            showStartingTime.isBefore(WorkingHours.OPENED_FROM) -> {
                throw IllegalStateException("Room Event can't start before ${WorkingHours.OPENED_FROM}")
            }

            showEndingTime.isAfter(WorkingHours.OPENED_TO) -> {
                throw IllegalStateException("Room Event can't end after ${WorkingHours.OPENED_TO}")
            }
        }
    }

    fun validatePremiereShowHours(
        showStartingTime: LocalTime,
        showEndingTime: LocalTime
    ) {
        when {
            showStartingTime.isBefore(WorkingHours.PREMIERE_FROM) -> {
                throw IllegalStateException("Premiere can't start before ${WorkingHours.PREMIERE_FROM}")
            }

            showEndingTime.isAfter(WorkingHours.PREMIERE_TO) -> {
                throw IllegalStateException("Premiere can't end after ${WorkingHours.PREMIERE_TO}")
            }
        }
    }
}