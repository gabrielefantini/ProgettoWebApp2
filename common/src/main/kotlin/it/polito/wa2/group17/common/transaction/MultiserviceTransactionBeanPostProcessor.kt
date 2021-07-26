package it.polito.wa2.group17.common.transaction

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component

@Component
class MultiserviceTransactionBeanPostProcessor : BeanPostProcessor {

    @Autowired
    private lateinit var multiserviceTransactionLinker: MultiserviceTransactionLinker

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        bean.javaClass.declaredMethods
            .filter { it.isAnnotationPresent(Rollback::class.java) }
            .forEach { method ->
                val transactionName = Rollback.extractTransactionName(method)
                multiserviceTransactionLinker.registerRollbackFor(transactionName, method)
            }
        return bean
    }
}
