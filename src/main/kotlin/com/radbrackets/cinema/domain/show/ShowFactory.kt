package com.radbrackets.cinema.domain.show

import com.radbrackets.cinema.domain.RoomEventValidator
import com.radbrackets.cinema.domain.Show
import com.radbrackets.cinema.domain.Show.ShowType
import com.radbrackets.cinema.movie.Movie
import com.radbrackets.cinema.room.Room
import com.radbrackets.cinema.room.availability.RoomAvailabilityChecker
import com.radbrackets.cinema.util.TimeRange
import java.time.LocalDateTime
import java.util.UUID

internal class ShowFactory(
    private val roomAvailabilityChecker: RoomAvailabilityChecker,
) {

    fun createShow(
        movie: Movie,
        room: Room,
        type: ShowType,
        startingTime: LocalDateTime
    ): Show {
        val showEndingTime = startingTime.plus(movie.duration)

        Validator(roomAvailabilityChecker).validate(
            roomId = room.id,
            showType = type,
            timeRange = TimeRange(from = startingTime, to = showEndingTime)
        )

        return Show(
            timeRange = TimeRange(startingTime, showEndingTime),
            roomId = room.id,
            movieId = movie.id,
            showType = type,
            is3DGlassesRequired = movie.is3DGlassesRequired
        )
    }

    private class Validator(
        private val roomAvailabilityChecker: RoomAvailabilityChecker
    ) {
        fun validate(
            roomId: UUID,
            showType: ShowType,
            timeRange: TimeRange
        ) {
            val validator = RoomEventValidator(roomAvailabilityChecker)

            validator.validateRoomAvailability(roomId, timeRange)

            when (showType) {
                ShowType.REGULAR -> validator.validateWorkingHours(
                    timeRange.from.toLocalTime(),
                    timeRange.to.toLocalTime()
                )

                ShowType.PREMIERE -> validator.validatePremiereShowHours(
                    timeRange.from.toLocalTime(),
                    timeRange.to.toLocalTime()
                )
            }
        }
    }

}