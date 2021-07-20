package it.polito.wa2.group17.common.connector

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryRegistry
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


annotation class RemoteCaller

@Component
@Aspect
class CircuitBreakerAspect {

    @Autowired
    private lateinit var circuitBreakerRegistry: CircuitBreakerRegistry

    @Autowired
    private lateinit var retryRegistry: RetryRegistry

    @Around("@annotation(it.polito.wa2.group17.common.connector.RemoteCaller)")
    fun connectorAspect(proceedingJoinPoint: ProceedingJoinPoint): Any? {
        val id = proceedingJoinPoint.signature.name
        val circuit = circuitBreakerRegistry.circuitBreaker(id)
        val retryContext = retryRegistry.retry(id)
        val circuitWrapped = circuit.decorateCallable { proceedingJoinPoint.proceed() }
        val retryWrapped = Retry.decorateCallable(retryContext) {
            circuitWrapped.call()
        }
        return retryWrapped.call()
    }
}

