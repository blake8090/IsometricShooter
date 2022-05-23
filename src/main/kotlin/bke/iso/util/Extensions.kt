package bke.iso.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun getLogger(obj: Any): Logger =
    LoggerFactory.getLogger(obj::class.java)
