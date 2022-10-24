package com.radbrackets.cinema.movie

import com.radbrackets.cinema.util.logger
import java.util.UUID

internal sealed interface MovieRepository {

    fun getMovie(id: UUID): Movie
    fun save(movie: Movie): Movie
    fun deleteAll()
}

internal class MovieRepositoryInMemory : MovieRepository {
    private val movies: MutableSet<Movie> = mutableSetOf()

    override fun getMovie(id: UUID): Movie {
        return movies.singleOrNull { it.id == id } ?: throw MovieNotFoundException(id)
    }

    override fun save(movie: Movie): Movie {
        if (movies.any { it.id == movie.id }) {
            javaClass.logger.info("Movie with id=${movie.id} already exists, overriding!")
        }

        movies.add(movie)

        return movie
    }


    override fun deleteAll() = movies.clear()
}