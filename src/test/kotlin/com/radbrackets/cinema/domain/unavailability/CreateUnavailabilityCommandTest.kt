package com.radbrackets.cinema.domain.unavailability

import com.radbrackets.cinema.domain.CleaningSlot
import com.radbrackets.cinema.domain.RoomEvent
import com.radbrackets.cinema.domain.RoomEventRepository
import com.radbrackets.cinema.domain.Unavailability.UnavailabilityReason.RENT
import com.radbrackets.cinema.room.Room
import com.radbrackets.cinema.room.RoomNotFoundException
import com.radbrackets.cinema.room.RoomRepositoryInMemory
import com.radbrackets.cinema.room.availability.RoomAvailabilityChecker
import com.radbrackets.cinema.util.TimeRange
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

internal class CreateUnavailabilityCommandTest {

    private val roomRepository = RoomRepositoryInMemory()
    private val roomEventRepository = RoomEventRepository()
    private val roomAvailabilityChecker = RoomAvailabilityChecker(roomEventRepository)
    private val unavailabilityFactory = UnavailabilityFactory(roomAvailabilityChecker)

    private val handler = CreateUnavailabilityCommand.CreateUnavailabilityCommandHandler(
        roomRepository = roomRepository,
        roomEventRepository = roomEventRepository,
        unavailabilityFactory = unavailabilityFactory
    )

    companion object {
        private val TIME_2022_10_20_9_15 = LocalDateTime.of(2022, 10, 20, 9, 15)
    }

    @Test
    internal fun `can't create unavailability when room not found`() {
        //given
        val roomId = UUID.randomUUID()

        //when //then
        assertThrows<RoomNotFoundException> {
            handler.handle(
                CreateUnavailabilityCommand(
                    reason = RENT,
                    roomId = roomId,
                    timeRange = TimeRange(
                        from = TIME_2022_10_20_9_15,
                        to = TIME_2022_10_20_9_15.plusMinutes(5)
                    )
                )
            )
        }
    }

    @Test
    internal fun `can successfully save unavailability`() {
        //given
        val room = addRoom()
        val timeRange = TimeRange(
            from = TIME_2022_10_20_9_15,
            to = TIME_2022_10_20_9_15.plusMinutes(15)
        )

        //when
        handler.handle(
            CreateUnavailabilityCommand(
                reason = RENT,
                roomId = room.id,
                timeRange = timeRange
            )
        )

        //then
        val roomEvents = roomEventRepository.getByRoomOnDay(room.id, timeRange.from.toLocalDate())
        assertEquals(1, roomEvents.size)
        roomEvents.single().apply {
            assertEquals(RoomEvent.Type.UNAVAILABILITY, roomEventType)
            assertEquals(timeRange, this.timeRange)
        }
    }

    @Test
    internal fun `can't create unavailability when room is unavailable at that time`() {
        //given
        val room = addRoom()
        val existingUnavailabilityTimeRange = TimeRange(
            from = TIME_2022_10_20_9_15,
            to = TIME_2022_10_20_9_15.plusMinutes(40)
        )

        roomEventRepository.save(CleaningSlot(room.id, existingUnavailabilityTimeRange))

        //when //then
        assertThrows<IllegalStateException> {
            handler.handle(
                CreateUnavailabilityCommand(
                    reason = RENT,
                    roomId = room.id,
                    timeRange = TimeRange(
                        from = existingUnavailabilityTimeRange.from.plusMinutes(10),
                        to = existingUnavailabilityTimeRange.from.plusMinutes(60)
                    )
                )
            )
        }
    }

    @Test
    internal fun `can't create unavailability starting before working hours`() {
        //given
        val room = addRoom()

        //when //then
        assertThrows<IllegalStateException> {
            handler.handle(
                CreateUnavailabilityCommand(
                    reason = RENT,
                    roomId = room.id,
                    timeRange = TimeRange(
                        from = LocalDateTime.of(2022, 10, 20, 7, 59),
                        to = LocalDateTime.of(2022, 10, 20, 10, 59)
                    )
                )
            )
        }
    }

    @Test
    internal fun `can create unavailability starting with working hours`() {
        //given
        val room = addRoom()

        assertDoesNotThrow {
            handler.handle(
                CreateUnavailabilityCommand(
                    reason = RENT,
                    roomId = room.id,
                    timeRange = TimeRange(
                        from = LocalDateTime.of(2022, 10, 20, 8, 0),
                        to = LocalDateTime.of(2022, 10, 20, 10, 0)
                    )
                )
            )
        }
    }

    @Test
    internal fun `can't create unavailability ending after working hours`() {
        //given
        val room = addRoom(Duration.ofMinutes(15))

        //when //then
        assertThrows<IllegalStateException> {
            handler.handle(
                CreateUnavailabilityCommand(
                    reason = RENT,
                    roomId = room.id,
                    timeRange = TimeRange(
                        from = LocalDateTime.of(2022, 10, 20, 19, 0),
                        to = LocalDateTime.of(2022, 10, 20, 22, 1)
                    )
                )
            )
        }
    }

    @Test
    internal fun `can create unavailability ending with working hours`() {
        //given
        val room = addRoom(Duration.ofMinutes(15))

        assertDoesNotThrow {
            handler.handle(
                CreateUnavailabilityCommand(
                    reason = RENT,
                    roomId = room.id,
                    timeRange = TimeRange(
                        from = LocalDateTime.of(2022, 10, 20, 19, 0),
                        to = LocalDateTime.of(2022, 10, 20, 21, 45)
                    )
                )
            )
        }
    }

    private fun addRoom(
        cleaningSlot: Duration = Room.DEFAULT_CLEANING_SLOT
    ) = roomRepository.save(Room(name = "1", cleaningSlot = cleaningSlot))
}