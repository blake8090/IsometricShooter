package bke.iso.engine.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Any.getLogger(): Logger =
    LoggerFactory.getLogger(this::class.java)

