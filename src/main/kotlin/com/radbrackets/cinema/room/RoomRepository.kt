package com.radbrackets.cinema.room

import java.util.UUID

internal sealed interface RoomRepository {
    fun save(room: Room): Room
    fun getRoom(roomId: UUID): Room
    fun deleteAll()
}

internal class RoomRepositoryInMemory : RoomRepository {
    private val rooms: MutableSet<Room> = mutableSetOf()

    override fun save(room: Room): Room {
        rooms.add(room)

        return room
    }

    override fun getRoom(roomId: UUID): Room {
        return rooms.singleOrNull { it.id == roomId } ?: throw RoomNotFoundException(roomId)
    }

    override fun deleteAll() = rooms.clear()

}