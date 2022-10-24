package com.radbrackets.cinema.room

import java.util.UUID

internal class RoomNotFoundException(roomId: UUID) : RuntimeException("Room with id=$roomId not found!")