package com.radbrackets.cinema.domain.cleaning

import com.radbrackets.cinema.domain.RoomEventRepository
import com.radbrackets.cinema.room.RoomRepository
import com.radbrackets.cinema.util.Command
import com.radbrackets.cinema.util.CommandHandler
import com.radbrackets.cinema.util.TimeRange
import java.time.LocalDateTime
import java.util.UUID

internal data class CreateCleaningSlotCommand(
    val roomId: UUID,
    val startingTime: LocalDateTime
) : Command {
    internal class CreateCleaningSlotCommandHandler(
        private val roomRepository: RoomRepository,
        private val roomEventRepository: RoomEventRepository,
        private val cleaningSlotFactory: CleaningSlotFactory
    ) : CommandHandler<CreateCleaningSlotCommand> {

        override fun handle(command: CreateCleaningSlotCommand) {
            val room = roomRepository.getRoom(command.roomId)

            val cleaningSlot = cleaningSlotFactory.create(
                roomId = command.roomId,
                timeRange = TimeRange(
                    from = command.startingTime,
                    to = command.startingTime.plus(room.cleaningSlot)
                )
            )

            roomEventRepository.save(cleaningSlot)
        }
    }

}