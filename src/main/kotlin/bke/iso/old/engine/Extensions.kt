package bke.iso.old.engine

import org.slf4j.Logger
import org.slf4j.LoggerFactory

val <T : Any> T.log: Logger
    get() = LoggerFactory.getLogger(this::class.java)