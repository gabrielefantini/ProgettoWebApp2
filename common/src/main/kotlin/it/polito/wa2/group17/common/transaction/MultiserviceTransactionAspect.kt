package it.polito.wa2.group17.common.transaction

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
                invokingMethod.getAnnotation(MultiserviceTransactional::class.java).transactionName
            )

        val invokingMethodReturnType = invokingMethod.returnType
        val hasReturnType = !invokingMethodReturnType.equals(Unit::class.java)
        val invokingMethodParamTypes = invokingMethod.parameterTypes
        val rollbackParamTypes = rollback.parameterTypes

        if ((rollbackParamTypes.size + if (hasReturnType) 1 else 0) != invokingMethodParamTypes.size)
            incompatibleRollbackException(rollback, invokingMethod)

        for ((i, param) in invokingMethodParamTypes.withIndex()) {
            if (!param.equals(rollbackParamTypes[i]))
                incompatibleRollbackException(rollback, invokingMethod)
        }

        if (hasReturnType && !rollbackParamTypes[invokingMethodParamTypes.size].equals(invokingMethodReturnType))
            incompatibleRollbackException(rollback, invokingMethod)


        return multiserviceTransactionSynchronizer.invokeWithinMultiserviceTransaction(
            rollback.apply { isAccessible = true },
            proceedingJoinPoint.args,
            proceedingJoinPoint.target,
            invokingMethod,
            hasReturnType
        ) { proceedingJoinPoint.proceed() }

    }

    private fun incompatibleRollbackException(rollback: Method, invokingMethod: Method) {
        throw IllegalStateException("Rollback parameters of $rollback does not match with the ones of the transaction $invokingMethod")

    }
}
