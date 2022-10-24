package com.radbrackets.cinema.domain.query

import com.radbrackets.cinema.domain.RoomEvent
import com.radbrackets.cinema.domain.Show
import com.radbrackets.cinema.domain.Unavailability
import com.radbrackets.cinema.movie.Is3DGlassesRequired
import com.radbrackets.cinema.util.TimeRange
import java.util.UUID

internal sealed class TimeRangedCinemaEventDto {
    abstract val id: UUID
    abstract val timeRange: TimeRange
    abstract val eventType: RoomEvent.Type
}

internal data class ShowDto(
    override val id: UUID,
    override val timeRange: TimeRange,
    val movieId: UUID,
    val is3DGlassesRequired: Is3DGlassesRequired,
    val showType: Show.ShowType
) : TimeRangedCinemaEventDto() {
    override val eventType: RoomEvent.Type = RoomEvent.Type.SHOW
}

internal data class CleaningSlotDto(
    override val id: UUID,
    override val timeRange: TimeRange
) : TimeRangedCinemaEventDto() {
    override val eventType: RoomEvent.Type = RoomEvent.Type.CLEANING
}

internal data class UnavailabilityDto(
    override val id: UUID,
    override val timeRange: TimeRange,
    val reason: Unavailability.UnavailabilityReason,
) : TimeRangedCinemaEventDto() {
    override val eventType: RoomEvent.Type = RoomEvent.Type.UNAVAILABILITY
}