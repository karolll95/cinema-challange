package com.radbrackets.cinema.domain

import java.time.LocalDate
import java.util.UUID

internal class RoomEventRepository(
    private val showRepository: ShowRepository = ShowRepository(),
    private val cleaningSlotRepository: CleaningSlotRepository = CleaningSlotRepository(),
    private val unavailabilityRepository: UnavailabilityRepository = UnavailabilityRepository()
) {

    fun save(show: Show) = showRepository.save(show)

    fun save(cleaningSlot: CleaningSlot) = cleaningSlotRepository.save(cleaningSlot)

    fun save(unavailability: Unavailability) = unavailabilityRepository.save(unavailability)

    fun getByRoomOnDay(roomId: UUID, from: LocalDate): List<RoomEvent> = showRepository.getByRoomOnDay(roomId, from) +
            cleaningSlotRepository.getByRoomOnDay(roomId, from) +
            unavailabilityRepository.getByRoomOnDay(roomId, from)

    fun getAllForDays(days: List<LocalDate>): List<RoomEvent> = showRepository.getAllForDays(days) +
            cleaningSlotRepository.getAllForDays(days) +
            unavailabilityRepository.getAllForDays(days)

    fun deleteAll() {
        showRepository.deleteAll()
        cleaningSlotRepository.deleteAll()
        unavailabilityRepository.deleteAll()
    }

}

//E.g. If it will be normal DB, all queries will have to consider only type == SHOW
internal class ShowRepository {
    private val shows: MutableSet<Show> = mutableSetOf()

    fun save(show: Show): Show {
        shows.add(show)

        return show
    }

    fun getByRoomOnDay(roomId: UUID, day: LocalDate) = shows.filter { it.roomId == roomId }
        .filter { it.timeRange.from.toLocalDate() == day }

    fun getAllForDays(days: List<LocalDate>): List<Show> {
        return shows.filter {
            days.contains(it.timeRange.from.toLocalDate()) &&
                    days.contains(it.timeRange.to.toLocalDate())
        }
    }

    fun deleteAll() = shows.clear()
}

//E.g. If it will be normal DB, all queries will have to consider only type == CLEANING
internal class CleaningSlotRepository {
    private val cleaningSlots: MutableSet<CleaningSlot> = mutableSetOf()

    fun save(cleaningSlot: CleaningSlot): CleaningSlot {
        cleaningSlots.add(cleaningSlot)

        return cleaningSlot
    }

    fun getByRoomOnDay(roomId: UUID, day: LocalDate) = cleaningSlots.filter { it.roomId == roomId }
        .filter { it.timeRange.from.toLocalDate() == day }

    fun getAllForDays(days: List<LocalDate>): List<CleaningSlot> {
        return cleaningSlots.filter {
            days.contains(it.timeRange.from.toLocalDate()) &&
                    days.contains(it.timeRange.to.toLocalDate())
        }
    }

    fun deleteAll() = cleaningSlots.clear()

}

//E.g. If it will be normal DB, all queries will have to consider only type == UNAVAILABLE
internal class UnavailabilityRepository {
    private val unavailabilities: MutableSet<Unavailability> = mutableSetOf()

    fun save(unavailability: Unavailability): Unavailability {
        unavailabilities.add(unavailability)

        return unavailability
    }

    fun getByRoomOnDay(roomId: UUID, day: LocalDate) = unavailabilities.filter { it.roomId == roomId }
        .filter { it.timeRange.from.toLocalDate() == day }

    fun getAllForDays(days: List<LocalDate>): List<Unavailability> {
        return unavailabilities.filter {
            days.contains(it.timeRange.from.toLocalDate()) &&
                    days.contains(it.timeRange.to.toLocalDate())
        }
    }

    fun deleteAll() = unavailabilities.clear()

}