package bke.iso.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

// TODO: switch to Any.getLogger()
fun getLogger(obj: Any): Logger =
    LoggerFactory.getLogger(obj::class.java)
