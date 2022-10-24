package com.radbrackets.cinema.domain.cleaning

import com.radbrackets.cinema.domain.CleaningSlot
import com.radbrackets.cinema.domain.RoomEvent
import com.radbrackets.cinema.domain.RoomEventRepository
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
import java.time.LocalDateTime.now
import java.time.LocalDateTime.of
import java.util.UUID

internal class CreateCleaningSlotCommandTest {

    private val roomRepository = RoomRepositoryInMemory()
    private val roomEventRepository = RoomEventRepository()
    private val roomAvailabilityChecker = RoomAvailabilityChecker(roomEventRepository)
    private val cleaningSlotFactory = CleaningSlotFactory(roomAvailabilityChecker)

    private val handler = CreateCleaningSlotCommand.CreateCleaningSlotCommandHandler(
        roomRepository = roomRepository,
        roomEventRepository = roomEventRepository,
        cleaningSlotFactory = cleaningSlotFactory
    )

    @Test
    internal fun `can't create cleaning slot when room not found`() {
        //given
        val roomId = UUID.randomUUID()

        //when //then
        assertThrows<RoomNotFoundException> {
            handler.handle(CreateCleaningSlotCommand(roomId, now()))
        }
    }

    @Test
    internal fun `can successfully save cleaning slot`() {
        //given
        val room = roomRepository.save(Room(name = "1"))
        val startingTime = now().minusHours(2)

        //when
        handler.handle(CreateCleaningSlotCommand(room.id, startingTime))

        //then
        val roomEvents = roomEventRepository.getByRoomOnDay(room.id, startingTime.toLocalDate())
        assertEquals(1, roomEvents.size)
        roomEvents.single().apply {
            assertEquals(RoomEvent.Type.CLEANING, roomEventType)
            assertEquals(TimeRange(startingTime, startingTime.plus(room.cleaningSlot)), timeRange)
        }
    }

    @Test
    internal fun `can't create cleaning slot when room is unavailable at that time`() {
        //given
        val room = roomRepository.save(Room(name = "1"))
        val existingUnavailabilityTimeRange = TimeRange(now().minusMinutes(120), now().minusMinutes(60))

        roomEventRepository.save(CleaningSlot(room.id, existingUnavailabilityTimeRange))

        //when //then
        assertThrows<IllegalStateException> {
            handler.handle(CreateCleaningSlotCommand(room.id, now().minusMinutes(90)))
        }
    }

    @Test
    internal fun `can't create cleaning slot starting before working hours`() {
        //given
        val room = roomRepository.save(Room(name = "1"))

        //when //then
        assertThrows<IllegalStateException> {
            handler.handle(CreateCleaningSlotCommand(room.id, of(2022, 10, 20, 7, 59)))
        }
    }

    @Test
    internal fun `can create cleaning slot starting with working hours`() {
        //given
        val room = roomRepository.save(Room(name = "1"))

        assertDoesNotThrow {
            handler.handle(CreateCleaningSlotCommand(room.id, of(2022, 10, 20, 8, 0)))
        }
    }

    @Test
    internal fun `can't create cleaning slot ending after working hours`() {
        //given
        val room = roomRepository.save(Room(name = "1", cleaningSlot = Duration.ofMinutes(15)))

        //when //then
        assertThrows<IllegalStateException> {
            handler.handle(CreateCleaningSlotCommand(room.id, of(2022, 10, 20, 21, 46)))
        }
    }

    @Test
    internal fun `can create cleaning slot ending with working hours`() {
        //given
        val room = roomRepository.save(Room(name = "1", cleaningSlot = Duration.ofMinutes(15)))

        assertDoesNotThrow {
            handler.handle(CreateCleaningSlotCommand(room.id, of(2022, 10, 20, 21, 45)))
        }
    }
}