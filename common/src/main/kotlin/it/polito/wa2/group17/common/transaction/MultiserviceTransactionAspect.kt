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
    private lateinit var transactionInvoker: TransactionInvoker


    @Autowired
    private lateinit var multiserviceTransactionSynchronizer: MultiserviceTransactionSynchronizer

    @Around("@annotation(MultiserviceTransactional)")
    fun filterEnabledUsersAccess(proceedingJoinPoint: ProceedingJoinPoint): Any {
        val invokingMethod = (proceedingJoinPoint.signature as MethodSignature).method

        val rollback =
            multiserviceTransactionLinker.getRollbackFor(
                invokingMethod.getAnnotation(MultiserviceTransactional::class.java).transactionName
            )

        try {
            return proceedingJoinPoint.proceed()
        } catch (t: Throwable) {
            //perform rollback
            throw t
        }
    }
}
