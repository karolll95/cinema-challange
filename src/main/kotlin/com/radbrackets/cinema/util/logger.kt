package com.radbrackets.cinema.util

import java.util.logging.Logger

val Class<*>.logger: Logger get() = Logger.getLogger(this.name)
