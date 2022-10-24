package com.radbrackets.cinema.domain.query

import com.radbrackets.cinema.domain.*
import com.radbrackets.cinema.domain.query.CinemaBoard.RoomPlan
import com.radbrackets.cinema.util.Query
import com.radbrackets.cinema.util.QueryHandler
import java.time.LocalDate

internal data class GetCinemaBoardQuery(
    val days: List<LocalDate>
) : Query<CinemaBoard> {

    internal class GetShowsByDaysQueryHandler(
        private val roomEventRepository: RoomEventRepository
    ) : QueryHandler<GetCinemaBoardQuery, CinemaBoard> {

        override fun handle(query: GetCinemaBoardQuery): CinemaBoard {
            val eventsByRoom = roomEventRepository.getAllForDays(query.days).groupBy { it.roomId }

            return CinemaBoard(
                board = eventsByRoom.map { entry ->
                    RoomPlan(
                        roomId = entry.key,
                        events = entry.value.map { event -> event.toDto() }.sortedBy { it.timeRange.from }
                    )
                }
            )
        }

        private fun RoomEvent.toDto(): TimeRangedCinemaEventDto {
            return when (this) {
                is Show -> ShowDto(
                    id = id,
                    timeRange = timeRange,
                    movieId = movieId,
                    is3DGlassesRequired = is3DGlassesRequired,
                    showType = showType
                )

                is CleaningSlot -> CleaningSlotDto(
                    id = id,
                    timeRange = timeRange
                )

                is Unavailability -> UnavailabilityDto(
                    id = id,
                    timeRange = timeRange,
                    reason = reason
                )
            }
        }
    }

}