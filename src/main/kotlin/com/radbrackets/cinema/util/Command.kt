package com.radbrackets.cinema.util

interface Command

interface CommandHandler<TIn : Command> {
    fun handle(command: TIn)
}