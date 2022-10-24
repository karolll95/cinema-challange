package com.radbrackets.cinema.room

import com.radbrackets.cinema.domain.BaseEntity
import java.time.Duration
import java.util.UUID

internal class Room(
    var name: String,
    var cleaningSlot: Duration = DEFAULT_CLEANING_SLOT,
    override val id: UUID = UUID.randomUUID()
) : BaseEntity {
    companion object {
        val DEFAULT_CLEANING_SLOT: Duration = Duration.ofMinutes(15)
    }
}