package com.radbrackets.cinema.room.availability

import com.radbrackets.cinema.domain.RoomEventRepository
import com.radbrackets.cinema.util.TimeRange
import java.util.UUID

internal class RoomAvailabilityChecker(
    private val roomEventRepository: RoomEventRepository
) {
    fun isAvailable(
        roomId: UUID,
        timeRange: TimeRange
    ): Available {
        val events = roomEventRepository.getByRoomOnDay(roomId, timeRange.from.toLocalDate())

        return Available(events.none { it.isTimeRangeOverlappingWith(timeRange) })
    }
}