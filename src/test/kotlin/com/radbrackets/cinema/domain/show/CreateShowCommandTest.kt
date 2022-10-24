package com.radbrackets.cinema.domain.show

import com.radbrackets.cinema.domain.CleaningSlot
import com.radbrackets.cinema.domain.RoomEventRepository
import com.radbrackets.cinema.domain.Show
import com.radbrackets.cinema.domain.Show.ShowType
import com.radbrackets.cinema.domain.Unavailability
import com.radbrackets.cinema.domain.cleaning.CleaningSlotFactory
import com.radbrackets.cinema.domain.cleaning.CreateCleaningSlotCommand
import com.radbrackets.cinema.movie.Movie
import com.radbrackets.cinema.movie.MovieNotFoundException
import com.radbrackets.cinema.movie.MovieRepositoryInMemory
import com.radbrackets.cinema.room.Room
import com.radbrackets.cinema.room.RoomNotFoundException
import com.radbrackets.cinema.room.RoomRepositoryInMemory
import com.radbrackets.cinema.room.availability.RoomAvailabilityChecker
import com.radbrackets.cinema.util.TimeRange
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.time.LocalDateTime

internal class CreateShowCommandTest {

    private val movieRepository = MovieRepositoryInMemory()
    private val roomRepository = RoomRepositoryInMemory()
    private val roomEventRepository = RoomEventRepository()

    private val roomAvailabilityChecker = RoomAvailabilityChecker(roomEventRepository)

    private val showFactory = ShowFactory(roomAvailabilityChecker)
    private val cleaningSlotFactory = CleaningSlotFactory(roomAvailabilityChecker)

    private val createCleaningSlotCommandHandler = CreateCleaningSlotCommand.CreateCleaningSlotCommandHandler(
        roomRepository = roomRepository,
        roomEventRepository = roomEventRepository,
        cleaningSlotFactory = cleaningSlotFactory
    )

    private val handler = CreateShowCommand.CreateShowCommandHandler(
        movieRepository = movieRepository,
        roomRepository = roomRepository,
        showFactory = showFactory,
        roomEventRepository = roomEventRepository,
        createCleaningSlotCommandHandler = createCleaningSlotCommandHandler
    )

    companion object {
        private val DATE_2022_10_20_16_00 = LocalDateTime.of(2022, 10, 20, 16, 0)
        private val MINUTES_120 = Duration.ofMinutes(120)
        private const val ROOM_NAME = "1"
    }

    @AfterEach
    internal fun tearDown() {
        roomEventRepository.deleteAll()
        movieRepository.deleteAll()
        roomRepository.deleteAll()
    }

    @Test
    internal fun `can't create show when movie not found`() {
        //given
        val movie = Movie(duration = MINUTES_120)
        val room = Room(name = ROOM_NAME)

        //when //then
        assertThrows<MovieNotFoundException> {
            handler.handle(
                CreateShowCommand(
                    movieId = movie.id,
                    roomId = room.id,
                    startingDate = DATE_2022_10_20_16_00,
                    type = ShowType.REGULAR
                )
            )
        }
    }

    @Test
    internal fun `can't create show when room not found`() {
        //given
        val movie = addMovie()
        val room = Room(name = ROOM_NAME)

        //when //then
        assertThrows<RoomNotFoundException> {
            handler.handle(
                CreateShowCommand(
                    movieId = movie.id,
                    roomId = room.id,
                    startingDate = DATE_2022_10_20_16_00,
                    type = ShowType.REGULAR
                )
            )
        }
    }

    @Test
    internal fun `when show successfully created, create cleaning slot`() {
        //given
        val startingDate = DATE_2022_10_20_16_00
        val movie = addMovie()
        val room = addRoom()

        //when
        handler.handle(
            CreateShowCommand(
                movieId = movie.id,
                roomId = room.id,
                startingDate = startingDate,
                type = ShowType.REGULAR
            )
        )

        //then
        val events = roomEventRepository.getByRoomOnDay(room.id, startingDate.toLocalDate())
        assertEquals(2, events.size)
        assertEquals(1, events.filterIsInstance(Show::class.java).size)
        assertEquals(1, events.filterIsInstance(CleaningSlot::class.java).size)
    }

    @Test
    internal fun `can't create show to unavailable room`() {
        //given
        val startingDate = DATE_2022_10_20_16_00
        val movie = addMovie()
        val room = addRoom()

        roomEventRepository.save(
            Unavailability(
                reason = Unavailability.UnavailabilityReason.RENT,
                timeRange = TimeRange(
                    from = LocalDateTime.of(2022, 10, 20, 14, 30),
                    to = LocalDateTime.of(2022, 10, 20, 17, 0)
                ),
                roomId = room.id
            )
        )

        //when //then
        assertThrows<IllegalStateException> {
            handler.handle(
                CreateShowCommand(
                    movieId = movie.id,
                    roomId = room.id,
                    startingDate = startingDate,
                    type = ShowType.REGULAR
                )
            )
        }
    }

    @Test
    internal fun `can successfully create show`() {
        //given
        val startingDate = DATE_2022_10_20_16_00
        val movie = addMovie()
        val room = addRoom()

        //when
        handler.handle(
            CreateShowCommand(
                movieId = movie.id,
                roomId = room.id,
                startingDate = startingDate,
                type = ShowType.REGULAR
            )
        )

        //then
        val events = roomEventRepository.getByRoomOnDay(room.id, startingDate.toLocalDate())
        assertEquals(2, events.size)
        assertEquals(1, events.filterIsInstance(Show::class.java).size)
        assertEquals(1, events.filterIsInstance(CleaningSlot::class.java).size)

        events.filterIsInstance(Show::class.java).single().apply {
            assertEquals(room.id, roomId)
            assertEquals(movie.id, movieId)
            assertEquals(startingDate, timeRange.from)
            assertEquals(startingDate.plus(movie.duration), timeRange.to)
            assertEquals(ShowType.REGULAR, showType)
        }
    }

    @Test
    internal fun `throws exception when regular show starts before working hours`() {
        //given
        val startingDate = LocalDateTime.of(2022, 10, 20, 7, 59)
        val movie = addMovie()
        val room = addRoom()

        //when //then
        assertThrows<IllegalStateException> {
            handler.handle(
                CreateShowCommand(
                    movieId = movie.id,
                    roomId = room.id,
                    startingDate = startingDate,
                    type = ShowType.REGULAR
                )
            )
        }
    }

    @Test
    internal fun `can create regular show starting with working hours`() {
        //given
        val startingDate = LocalDateTime.of(2022, 10, 20, 8, 0)
        val movie = addMovie()
        val room = addRoom()

        // when //then
        assertDoesNotThrow {
            handler.handle(
                CreateShowCommand(
                    movieId = movie.id,
                    roomId = room.id,
                    startingDate = startingDate,
                    type = ShowType.REGULAR
                )
            )
        }
    }

    @Test
    internal fun `throws exception when regular show ends after working hours`() {
        //given
        val startingDate = LocalDateTime.of(2022, 10, 20, 20, 1)
        val movie = addMovie()
        val room = addRoom()

        // when //then
        assertThrows<IllegalStateException> {
            handler.handle(
                CreateShowCommand(
                    movieId = movie.id,
                    roomId = room.id,
                    startingDate = startingDate,
                    type = ShowType.REGULAR
                )
            )
        }
    }

    @Test
    internal fun `can create regular show ending with working hours`() {
        //given
        val startingDate = LocalDateTime.of(2022, 10, 20, 19, 45)
        val movie = addMovie()
        val room = addRoom()

        // when //then
        assertDoesNotThrow {
            handler.handle(
                CreateShowCommand(
                    movieId = movie.id,
                    roomId = room.id,
                    startingDate = startingDate,
                    type = ShowType.REGULAR
                )
            )
        }
    }

    @Test
    internal fun `throws exception when premiere show starts before premiere hours`() {
        //given
        val startingDate = LocalDateTime.of(2022, 10, 20, 16, 59)
        val movie = addMovie()
        val room = addRoom()

        // when //then
        assertThrows<IllegalStateException> {
            handler.handle(
                CreateShowCommand(
                    movieId = movie.id,
                    roomId = room.id,
                    startingDate = startingDate,
                    type = ShowType.PREMIERE
                )
            )
        }
    }

    @Test
    internal fun `can create premiere show starting with premiere hours`() {
        //given
        val startingDate = LocalDateTime.of(2022, 10, 20, 17, 0)
        val movie = addMovie()
        val room = addRoom()

        // when //then
        assertDoesNotThrow {
            handler.handle(
                CreateShowCommand(
                    movieId = movie.id,
                    roomId = room.id,
                    startingDate = startingDate,
                    type = ShowType.PREMIERE
                )
            )
        }
    }

    @Test
    internal fun `throws exception when premiere show ends after premiere hours`() {
        //given
        val startingDate = LocalDateTime.of(2022, 10, 20, 19, 1)
        val movie = addMovie()
        val room = addRoom()

        // when //then
        assertThrows<IllegalStateException> {
            handler.handle(
                CreateShowCommand(
                    movieId = movie.id,
                    roomId = room.id,
                    startingDate = startingDate,
                    type = ShowType.PREMIERE
                )
            )
        }
    }

    @Test
    internal fun `can create premiere show ending with premiere hours`() {
        //given
        val startingDate = LocalDateTime.of(2022, 10, 20, 19, 0)
        val movie = addMovie()
        val room = addRoom()

        // when //then
        assertDoesNotThrow {
            handler.handle(
                CreateShowCommand(
                    movieId = movie.id,
                    roomId = room.id,
                    startingDate = startingDate,
                    type = ShowType.PREMIERE
                )
            )
        }
    }

    private fun addMovie() = movieRepository.save(Movie(duration = MINUTES_120))

    private fun addRoom() = roomRepository.save(Room(name = ROOM_NAME))
}