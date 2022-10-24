package com.radbrackets.cinema.domain.unavailability

import com.radbrackets.cinema.domain.RoomEventRepository
import com.radbrackets.cinema.domain.Unavailability.UnavailabilityReason
import com.radbrackets.cinema.room.RoomRepository
import com.radbrackets.cinema.util.TimeRange
import java.util.UUID

internal data class CreateUnavailabilityCommand(
    val reason: UnavailabilityReason,
    val timeRange: TimeRange,
    val roomId: UUID
) {
    internal class CreateUnavailabilityCommandHandler(
        private val roomRepository: RoomRepository,
        private val roomEventRepository: RoomEventRepository,
        private val unavailabilityFactory: UnavailabilityFactory
    ) {
        fun handle(command: CreateUnavailabilityCommand) {
            val room = roomRepository.getRoom(command.roomId)

            val unavailability = unavailabilityFactory.create(
                reason = command.reason,
                roomId = room.id,
                timeRange = command.timeRange
            )

            roomEventRepository.save(unavailability)
        }
    }
}