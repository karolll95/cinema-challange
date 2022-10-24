package com.radbrackets.cinema.domain.query

import java.util.UUID

internal data class CinemaBoard(
    val board: List<RoomPlan>
) {
    data class RoomPlan(
        val roomId: UUID,
        val events: List<TimeRangedCinemaEventDto>
    )
}