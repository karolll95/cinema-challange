package com.radbrackets.cinema.movie

import java.util.UUID

internal class MovieNotFoundException(movieId: UUID) : RuntimeException("Movie with id=$movieId not found!")