package com.radbrackets.cinema.domain.query

import com.radbrackets.cinema.domain.*
import com.radbrackets.cinema.movie.Is3DGlassesRequired
import com.radbrackets.cinema.util.TimeRange
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class GetCinemaBoardQueryTest {

    private val roomEventRepository = RoomEventRepository()

    private val handler = GetCinemaBoardQuery.GetShowsByDaysQueryHandler(
        roomEventRepository = roomEventRepository
    )

    companion object {
        private val ROOM_ID_1 = UUID.randomUUID()
        private val ROOM_ID_2 = UUID.randomUUID()

        private val DAY_2022_10_17 = LocalDate.of(2022, 10, 17)
        private val DAY_2022_10_18 = LocalDate.of(2022, 10, 18)
    }

    @AfterEach
    internal fun tearDown() {
        roomEventRepository.deleteAll()
    }

    @Test
    internal fun `returns empty cinema board when no events found`() {
        //given

        //when
        val result = handler.handle(GetCinemaBoardQuery(listOf(LocalDate.now())))

        //then
        assertTrue(result.board.isEmpty())
    }

    @Test
    internal fun `returns events matching given days`() {
        //given
        populateRoomEvents()
        val days = listOf(DAY_2022_10_17)

        //when
        val result = handler.handle(GetCinemaBoardQuery(days))

        //then
        assertTrue(result.board.flatMap { it.events }.isNotEmpty())
        assertTrue(result.board.all { it.events.all { it.timeRange.from.toLocalDate() == DAY_2022_10_17 } })
    }

    @Test
    internal fun `returns events for all rooms that have some events`() {
        //given
        populateRoomEvents()
        val days = listOf(DAY_2022_10_17)

        //when
        val result = handler.handle(GetCinemaBoardQuery(days))

        //then
        assertTrue(result.board.map { it.roomId }.containsAll(listOf(ROOM_ID_1, ROOM_ID_2)))
    }

    @Test
    internal fun `returns all types of events`() {
        //given
        populateRoomEvents()
        val days = listOf(DAY_2022_10_18)

        //when
        val result = handler.handle(GetCinemaBoardQuery(days))

        //then
        assertTrue(result.board.flatMap { it.events }.isNotEmpty())
        result.board
            .filter { it.roomId == ROOM_ID_2 }
            .flatMap { it.events }
            .apply {
                assertEquals(1, filter { it.eventType == RoomEvent.Type.UNAVAILABILITY }.size)
                assertEquals(2, filter { it.eventType == RoomEvent.Type.SHOW }.size)
                assertEquals(3, filter { it.eventType == RoomEvent.Type.CLEANING }.size)
            }
    }

    @Test
    internal fun `returns events ordered per day`() {
        //given
        populateRoomEvents()
        val days = listOf(DAY_2022_10_17)

        //when
        val result = handler.handle(GetCinemaBoardQuery(days))

        //then
        val events = result.board
            .filter { it.roomId == ROOM_ID_1 }
            .flatMap { it.events }
        assertEquals(DAY_2022_10_17.atTime(8, 0), events[0].timeRange.from)
        assertEquals(DAY_2022_10_17.atTime(10, 0), events[1].timeRange.from)
        assertEquals(DAY_2022_10_17.atTime(10, 15), events[2].timeRange.from)
        assertEquals(DAY_2022_10_17.atTime(12, 45), events[3].timeRange.from)
    }

    private fun populateRoomEvents() {
        //DAY_2022_10_17 ROOM_ID_1
        roomEventRepository.save(
            getShow(
                DAY_2022_10_17.atTime(8, 0),
                DAY_2022_10_17.atTime(10, 0),
                ROOM_ID_1
            )
        )
        roomEventRepository.save(
            getCleaningSlot(
                DAY_2022_10_17.atTime(10, 0),
                DAY_2022_10_17.atTime(10, 15),
                ROOM_ID_1
            )
        )
        roomEventRepository.save(
            getShow(
                DAY_2022_10_17.atTime(10, 15),
                DAY_2022_10_17.atTime(12, 45),
                ROOM_ID_1
            )
        )
        roomEventRepository.save(
            getCleaningSlot(
                DAY_2022_10_17.atTime(12, 45),
                DAY_2022_10_17.atTime(13, 0),
                ROOM_ID_1
            )
        )

        //DAY_2022_10_17 ROOM_ID_2
        roomEventRepository.save(
            getShow(
                DAY_2022_10_17.atTime(9, 0),
                DAY_2022_10_17.atTime(12, 0),
                ROOM_ID_2
            )
        )
        roomEventRepository.save(
            getCleaningSlot(
                DAY_2022_10_17.atTime(12, 0),
                DAY_2022_10_17.atTime(12, 30),
                ROOM_ID_2
            )
        )
        roomEventRepository.save(
            getShow(
                DAY_2022_10_17.atTime(16, 15),
                DAY_2022_10_17.atTime(20, 45),
                ROOM_ID_2
            )
        )
        roomEventRepository.save(
            getCleaningSlot(
                DAY_2022_10_17.atTime(20, 45),
                DAY_2022_10_17.atTime(21, 15),
                ROOM_ID_2
            )
        )

        //DAY_2022_10_18 ROOM_ID_1
        roomEventRepository.save(
            getUnavailability(
                DAY_2022_10_18.atTime(8, 0),
                DAY_2022_10_18.atTime(16, 0),
                ROOM_ID_1
            )
        )
        roomEventRepository.save(
            getCleaningSlot(
                DAY_2022_10_18.atTime(16, 0),
                DAY_2022_10_18.atTime(16, 15),
                ROOM_ID_1
            )
        )
        //DAY_2022_10_18 ROOM_ID_2
        roomEventRepository.save(
            getUnavailability(
                DAY_2022_10_18.atTime(8, 0),
                DAY_2022_10_18.atTime(8, 30),
                ROOM_ID_2
            )
        )
        roomEventRepository.save(
            getCleaningSlot(
                DAY_2022_10_18.atTime(8, 30),
                DAY_2022_10_18.atTime(9, 0),
                ROOM_ID_2
            )
        )
        roomEventRepository.save(
            getShow(
                DAY_2022_10_18.atTime(9, 0),
                DAY_2022_10_18.atTime(12, 0),
                ROOM_ID_2
            )
        )
        roomEventRepository.save(
            getCleaningSlot(
                DAY_2022_10_18.atTime(12, 0),
                DAY_2022_10_18.atTime(12, 30),
                ROOM_ID_2
            )
        )
        roomEventRepository.save(
            getShow(
                DAY_2022_10_18.atTime(16, 15),
                DAY_2022_10_18.atTime(20, 45),
                ROOM_ID_2
            )
        )
        roomEventRepository.save(
            getCleaningSlot(
                DAY_2022_10_18.atTime(20, 45),
                DAY_2022_10_18.atTime(21, 15),
                ROOM_ID_2
            )
        )

    }

    private fun getShow(
        from: LocalDateTime,
        to: LocalDateTime,
        roomId: UUID
    ) = Show(
        timeRange = TimeRange(from = from, to = to),
        roomId = roomId,
        movieId = UUID.randomUUID(),
        showType = Show.ShowType.REGULAR,
        is3DGlassesRequired = Is3DGlassesRequired(value = false)
    )

    private fun getCleaningSlot(
        from: LocalDateTime,
        to: LocalDateTime,
        roomId: UUID
    ) = CleaningSlot(
        roomId = roomId,
        timeRange = TimeRange(from = from, to = to)
    )

    private fun getUnavailability(
        from: LocalDateTime,
        to: LocalDateTime,
        roomId: UUID
    ) = Unavailability(
        reason = Unavailability.UnavailabilityReason.RENT,
        roomId = roomId,
        timeRange = TimeRange(from = from, to = to)
    )
}