package it.polito.wa2.group17.common.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface Loggable {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(javaClass)
    }
}
