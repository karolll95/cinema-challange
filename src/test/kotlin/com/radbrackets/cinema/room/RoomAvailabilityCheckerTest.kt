package com.radbrackets.cinema.room

import com.radbrackets.cinema.domain.RoomEventRepository
import com.radbrackets.cinema.domain.Unavailability
import com.radbrackets.cinema.room.availability.RoomAvailabilityChecker
import com.radbrackets.cinema.util.TimeRange
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

internal class RoomAvailabilityCheckerTest {

    private val repository = RoomEventRepository()
    private val validator = RoomAvailabilityChecker(repository)

    @AfterEach
    internal fun tearDown() {
        repository.deleteAll()
    }

    @Test
    internal fun `returns available when no unavailability for given room`() {
        //given
        val roomId = UUID.randomUUID()
        val from = LocalDateTime.of(2022, 10, 20, 16, 0)
        val to = LocalDateTime.of(2022, 10, 20, 18, 0)

        //when
        val result = validator.isAvailable(roomId, TimeRange(from, to))

        //then
        assertTrue(result.value)
    }

    @Test
    internal fun `returns available when unavailabilities for given room dont overlap with from to range`() {
        //given
        val roomId = UUID.randomUUID()

        addUnavailability(
            roomId = roomId,
            from = LocalDateTime.of(2022, 10, 20, 14, 30),
            to = LocalDateTime.of(2022, 10, 20, 15, 0)
        )
        addUnavailability(
            roomId = roomId,
            from = LocalDateTime.of(2022, 10, 20, 18, 1),
            to = LocalDateTime.of(2022, 10, 20, 22, 0)
        )

        //when
        val result = validator.isAvailable(
            roomId = roomId,
            timeRange = TimeRange(
                from = LocalDateTime.of(2022, 10, 20, 16, 0),
                to = LocalDateTime.of(2022, 10, 20, 18, 0)
            )
        )

        //then
        assertTrue(result.value)
    }

    @Test
    internal fun `returns unavailable when start of the show overlaps with unavailability`() {
        //given
        val roomId = UUID.randomUUID()

        addUnavailability(
            roomId = roomId,
            from = LocalDateTime.of(2022, 10, 20, 14, 30),
            to = LocalDateTime.of(2022, 10, 20, 15, 0)
        )
        addUnavailability(
            roomId = roomId,
            from = LocalDateTime.of(2022, 10, 20, 18, 1),
            to = LocalDateTime.of(2022, 10, 20, 22, 0)
        )

        //when
        val result = validator.isAvailable(
            roomId = roomId,
            timeRange = TimeRange(
                from = LocalDateTime.of(2022, 10, 20, 14, 45),
                to = LocalDateTime.of(2022, 10, 20, 18, 0)
            )
        )

        //then
        assertFalse(result.value)
    }

    @Test
    internal fun `returns unavailable when end of the show overlaps with unavailability`() {
        //given
        val roomId = UUID.randomUUID()

        addUnavailability(
            roomId = roomId,
            from = LocalDateTime.of(2022, 10, 20, 14, 30),
            to = LocalDateTime.of(2022, 10, 20, 15, 0)
        )
        addUnavailability(
            roomId = roomId,
            from = LocalDateTime.of(2022, 10, 20, 18, 1),
            to = LocalDateTime.of(2022, 10, 20, 22, 0)
        )

        //when
        val result = validator.isAvailable(
            roomId = roomId,
            timeRange = TimeRange(
                from = LocalDateTime.of(2022, 10, 20, 15, 45),
                to = LocalDateTime.of(2022, 10, 20, 19, 0)
            )
        )

        //then
        assertFalse(result.value)
    }

    @Test
    internal fun `returns available when show starts at the same minute unavailability ends`() {
        //given
        val roomId = UUID.randomUUID()

        addUnavailability(
            roomId = roomId,
            from = LocalDateTime.of(2022, 10, 20, 14, 30),
            to = LocalDateTime.of(2022, 10, 20, 15, 0)
        )
        addUnavailability(
            roomId = roomId,
            from = LocalDateTime.of(2022, 10, 20, 18, 1),
            to = LocalDateTime.of(2022, 10, 20, 22, 0)
        )

        //when
        val result = validator.isAvailable(
            roomId = roomId,
            timeRange = TimeRange(
                from = LocalDateTime.of(2022, 10, 20, 14, 30),
                to = LocalDateTime.of(2022, 10, 20, 17, 0)
            )
        )

        //then
        assertTrue(result.value)
    }

    @Test
    internal fun `returns available when show ends at the same minute unavailability starts`() {
        //given
        val roomId = UUID.randomUUID()

        addUnavailability(
            roomId = roomId,
            from = LocalDateTime.of(2022, 10, 20, 14, 30),
            to = LocalDateTime.of(2022, 10, 20, 15, 0)
        )
        addUnavailability(
            roomId = roomId,
            from = LocalDateTime.of(2022, 10, 20, 18, 0),
            to = LocalDateTime.of(2022, 10, 20, 22, 0)
        )

        //when
        val result = validator.isAvailable(
            roomId = roomId,
            timeRange = TimeRange(
                from = LocalDateTime.of(2022, 10, 20, 15, 45),
                to = LocalDateTime.of(2022, 10, 20, 18, 0)
            )
        )

        //then
        assertTrue(result.value)
    }

    private fun addUnavailability(
        roomId: UUID,
        from: LocalDateTime,
        to: LocalDateTime
    ) {
        repository.save(
            Unavailability(
                timeRange = TimeRange(
                    from = from,
                    to = to
                ),
                reason = Unavailability.UnavailabilityReason.PARTY,
                roomId = roomId
            )
        )
    }
}