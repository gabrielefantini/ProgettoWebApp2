package it.polito.wa2.group17.common.transaction

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Aspect
class MultiserviceTransactionAspect {

    @Autowired
    private lateinit var multiserviceTransactionLinker: MultiserviceTransactionLinker

    @Autowired
    private lateinit var multiserviceTransactionSynchronizer: MultiserviceTransactionSynchronizer

    @Around("@annotation(MultiserviceTransactional)")
    fun multiserviceTransaction(proceedingJoinPoint: ProceedingJoinPoint): Any? {
        val invokingMethod = (proceedingJoinPoint.signature as MethodSignature).method

        val rollback =
            multiserviceTransactionLinker.getRollbackFor(
                invokingMethod.getAnnotation(MultiserviceTransactional::class.java).transactionName
            )

        if (!rollback.parameterTypes.contentEquals(invokingMethod.parameterTypes)) {
            throw IllegalStateException("Rollback parameters of $rollback does not match with the ones of the transaction $invokingMethod")
        }

        return multiserviceTransactionSynchronizer.invokeWithinMultiserviceTransaction(
            rollback.apply { isAccessible = true },
            proceedingJoinPoint.args,
            proceedingJoinPoint.target
        ) { proceedingJoinPoint.proceed() }

    }
}
