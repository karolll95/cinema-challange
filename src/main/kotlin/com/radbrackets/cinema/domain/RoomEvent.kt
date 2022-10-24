package com.radbrackets.cinema.domain

import com.radbrackets.cinema.movie.Is3DGlassesRequired
import com.radbrackets.cinema.util.TimeRange
import java.util.UUID

internal sealed class RoomEvent : BaseEntity {
    enum class Type {
        SHOW, CLEANING, UNAVAILABILITY
    }

    abstract val roomId: UUID
    abstract val timeRange: TimeRange
    abstract val roomEventType: Type

    fun isTimeRangeOverlappingWith(timeRangeToCheck: TimeRange): Boolean {
        return timeRangeToCheck.from.isBefore(timeRange.to) && timeRangeToCheck.from.isAfter(timeRange.from) ||
                timeRangeToCheck.to.isBefore(timeRange.to) && timeRangeToCheck.to.isAfter(timeRange.from)
    }
}

internal data class Show(
    override val timeRange: TimeRange,
    override val roomId: UUID,
    val movieId: UUID,
    val showType: ShowType,
    val is3DGlassesRequired: Is3DGlassesRequired,
    override val id: UUID = UUID.randomUUID()
) : RoomEvent() {
    override val roomEventType: Type = Type.SHOW

    enum class ShowType {
        REGULAR, PREMIERE
    }
}

internal data class CleaningSlot(
    override val roomId: UUID,
    override val timeRange: TimeRange,
    override val id: UUID = UUID.randomUUID()
) : RoomEvent() {
    override val roomEventType: Type = Type.CLEANING
}

internal data class Unavailability(
    val reason: UnavailabilityReason,
    override val roomId: UUID,
    override val timeRange: TimeRange,
    override val id: UUID = UUID.randomUUID()
) : RoomEvent() {
    override val roomEventType: Type = Type.UNAVAILABILITY

    internal enum class UnavailabilityReason {
        RENT, PARTY
    }
}