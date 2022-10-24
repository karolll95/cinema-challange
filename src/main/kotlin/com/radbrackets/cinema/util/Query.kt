package com.radbrackets.cinema.util

interface Query<out TOut>

interface QueryHandler<in TIn : Query<TOut>, out TOut>  {
    fun handle(query: TIn): TOut
}