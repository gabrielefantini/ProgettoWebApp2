package it.polito.wa2.group17.common.transaction

import it.polito.wa2.group17.common.utils.reflection.getAllMethods
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component

@Component
class MultiserviceTransactionBeanPostProcessor : BeanPostProcessor {

    @Autowired
    private lateinit var multiserviceTransactionLinker: MultiserviceTransactionLinker

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        bean.javaClass.getAllMethods()
            .filter { it.isAnnotationPresent(Rollback::class.java) || it.isAnnotationPresent(MultiserviceTransactional::class.java) }
            .forEach { method ->
                if (method.isAnnotationPresent(Rollback::class.java)) {
                    val transactionName = Rollback.extractTransactionName(method)
                    multiserviceTransactionLinker.registerRollbackFor(transactionName, method)
                } else {
                    val transactionName = MultiserviceTransactional.extractTransactionName(method)
                    multiserviceTransactionLinker.registerTransactionFor(transactionName, method)
                }
            }
        return bean
    }
}
