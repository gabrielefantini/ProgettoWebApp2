package it.polito.wa2.group17.common.exception

import it.polito.wa2.group17.common.utils.reflection.CallSiteInspector
import org.slf4j.LoggerFactory

abstract class AutoLoggableException(message: String) : Exception(message) {
    init {
        /**
         * Get the logger of the first calling class outside the exception hierarchy!
         */
        LoggerFactory.getLogger(CallSiteInspector.getCallerClass { !Exception::class.java.isAssignableFrom(it) })
            .error(message)
    }
}
