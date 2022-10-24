package com.radbrackets.cinema.movie

import com.radbrackets.cinema.domain.BaseEntity
import java.time.Duration
import java.util.UUID

internal class Movie(
    val duration: Duration,
    val is3DGlassesRequired: Is3DGlassesRequired = Is3DGlassesRequired(false),
    override val id: UUID = UUID.randomUUID(),
) : BaseEntity