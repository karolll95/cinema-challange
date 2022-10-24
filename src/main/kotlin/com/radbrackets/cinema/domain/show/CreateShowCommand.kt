package com.radbrackets.cinema.domain.show

import com.radbrackets.cinema.domain.RoomEventRepository
import com.radbrackets.cinema.domain.Show
import com.radbrackets.cinema.domain.cleaning.CreateCleaningSlotCommand
import com.radbrackets.cinema.movie.MovieRepository
import com.radbrackets.cinema.room.RoomRepository
import com.radbrackets.cinema.util.Command
import com.radbrackets.cinema.util.CommandHandler
import java.time.LocalDateTime
import java.util.UUID

internal data class CreateShowCommand(
    val movieId: UUID,
    val roomId: UUID,
    val startingDate: LocalDateTime,
    val type: Show.ShowType
) : Command {
    internal class CreateShowCommandHandler(
        private val showFactory: ShowFactory,
        private val roomEventRepository: RoomEventRepository,
        private val movieRepository: MovieRepository,
        private val roomRepository: RoomRepository,
        private val createCleaningSlotCommandHandler: CreateCleaningSlotCommand.CreateCleaningSlotCommandHandler
    ) : CommandHandler<CreateShowCommand> {
        //Provides transaction

        //As from the requirements it seems that system is designed for single cinema, I'm assuming that it will be
        //used by couple of planners in the same time (worst case scenario), so I suppose simple, Kotlin based
        //synchronization mechanism is enough. If it will be a system for whole cinema group, then it should be adjusted
        //at model level (to differentiate specific cinema) and more sophisticated concurrency handling might be needed.
        //I'd consider Optimistic/Pessimistic locking or Event Sourcing.
        @Synchronized
        override fun handle(command: CreateShowCommand) {
            val movie = movieRepository.getMovie(command.movieId)
            val room = roomRepository.getRoom(command.roomId)

            val show = roomEventRepository.save(showFactory.createShow(movie, room, command.type, command.startingDate))

            //Creating cleaning slot is done synchronously, so that we can relay on atomicy of failing either
            //creating show or cleaning slot will revert the whole transaction
            createCleaningSlotCommandHandler.handle(CreateCleaningSlotCommand(room.id, show.timeRange.to))
        }


    }
}