package com.radbrackets.cinema.domain.cleaning

import com.radbrackets.cinema.domain.CleaningSlot
import com.radbrackets.cinema.domain.RoomEventValidator
import com.radbrackets.cinema.room.availability.RoomAvailabilityChecker
import com.radbrackets.cinema.util.TimeRange
import java.util.UUID

internal class CleaningSlotFactory(
    private val roomAvailabilityChecker: RoomAvailabilityChecker
) {
    fun create(
        roomId: UUID,
        timeRange: TimeRange
    ): CleaningSlot {
        Validator(roomAvailabilityChecker).validate(roomId, timeRange)

        return CleaningSlot(
            roomId = roomId,
            timeRange = timeRange
        )
    }

    private class Validator(
        private val roomAvailabilityChecker: RoomAvailabilityChecker
    ) {
        fun validate(
            roomId: UUID,
            timeRange: TimeRange
        ) {
            val validator = RoomEventValidator(roomAvailabilityChecker)

            validator.validateRoomAvailability(roomId, timeRange)
            validator.validateWorkingHours(timeRange.from.toLocalTime(), timeRange.to.toLocalTime())
        }
    }
}