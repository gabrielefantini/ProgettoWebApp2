package it.polito.wa2.group17.common.connector

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryRegistry
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Component

@Target(AnnotationTarget.CLASS)
@Component
annotation class Connector(@get:AliasFor(annotation = Component::class) val value: String = "")

@Component
@Aspect
class ConnectorAspect {

    @Autowired
    private lateinit var circuitBreakerRegistry: CircuitBreakerRegistry

    @Autowired
    private lateinit var retryRegistry: RetryRegistry

    @Around("@annotation(it.polito.wa2.group17.common.connector.Connector)")
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

