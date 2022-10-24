package com.radbrackets.cinema.domain.unavailability

import com.radbrackets.cinema.domain.RoomEventValidator
import com.radbrackets.cinema.domain.Unavailability
import com.radbrackets.cinema.room.availability.RoomAvailabilityChecker
import com.radbrackets.cinema.util.TimeRange
import java.util.UUID

internal class UnavailabilityFactory(
    private val roomAvailabilityChecker: RoomAvailabilityChecker
) {

    fun create(
        roomId: UUID,
        reason: Unavailability.UnavailabilityReason,
        timeRange: TimeRange
    ): Unavailability {
        Validator(roomAvailabilityChecker).validate(roomId, timeRange)

        return Unavailability(
            reason = reason,
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