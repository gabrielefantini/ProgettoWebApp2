package it.polito.wa2.group17.common.utils.reflection

import java.util.concurrent.atomic.AtomicInteger


object CallSiteInspector {

    private fun getCallerStackElement(candidateFilter: (StackTraceElement) -> Boolean = { true }): StackTraceElement? {
        val stElements = Thread.currentThread().stackTrace
        var callerClassName: String? = null
        for (i in 1 until stElements.size) {
            val ste = stElements[i]
            if (ste.className != CallSiteInspector::class.java.name && ste.className.indexOf("java.lang.Thread") != 0) {
                if (callerClassName == null) {
                    callerClassName = ste.className
                } else if (callerClassName != ste.className && candidateFilter(ste)) {
                    return ste
                }
            }
        }
        return null
    }

    fun getCallerClass(candidateFilter: (Class<*>) -> Boolean = { true }): Class<*>? =
        getCallerStackElement { candidateFilter(Class.forName(it.className)) }?.let { Class.forName(it.className) }

    fun getCallerMethodName(candidateFilter: (String) -> Boolean = { true }): String? =
        getCallerStackElement { candidateFilter(it.methodName) }?.methodName

    fun getCallerLine(candidateFilter: (StackTraceElement) -> Boolean = { true }): Int? =
        getCallerStackElement(candidateFilter)?.lineNumber

    private fun getCallerStackElementSkipping(count: Int): StackTraceElement? {
        if (count < 0)
            throw IllegalArgumentException("Cannot skip $count elements in stack trace!")
        val counter = AtomicInteger(0)
        return getCallerStackElement { counter.getAndIncrement() == count }
    }

    fun getCallerClassSkipping(count: Int): Class<*>? =
        getCallerStackElementSkipping(count)?.let { Class.forName(it.className) }

    fun getCallerMethodNameSkipping(count: Int): String? =
        getCallerStackElementSkipping(count)?.methodName

    fun getCallerLineSkipping(count: Int): Int? =
        getCallerStackElementSkipping(count)?.lineNumber

}
