package it.polito.wa2.group17.common.transaction

import it.polito.wa2.group17.common.utils.reflection.hasReturnType
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.lang.reflect.Method

@Component
@Aspect
class MultiserviceTransactionAspect {

    @Autowired
    private lateinit var multiserviceTransactionLinker: MultiserviceTransactionLinker

    @Autowired
    private lateinit var multiserviceTransactionSynchronizer: MultiserviceTransactionSynchronizer

    @Around("@annotation(it.polito.wa2.group17.common.transaction.MultiserviceTransactional)")
    fun multiserviceTransaction(proceedingJoinPoint: ProceedingJoinPoint): Any? {
        val invokingMethod = (proceedingJoinPoint.signature as MethodSignature).method

        val rollback =
            multiserviceTransactionLinker.getRollbackFor(
                MultiserviceTransactional.extractTransactionName(invokingMethod)
            )

        return multiserviceTransactionSynchronizer.invokeWithinMultiserviceTransaction(
            rollback.apply { isAccessible = true },
            proceedingJoinPoint.args,
            proceedingJoinPoint.target,
            invokingMethod,
            invokingMethod.hasReturnType()
        ) { proceedingJoinPoint.proceed() }

    }


}
