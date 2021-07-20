package it.polito.wa2.group17.common.transaction

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class MultiserviceTransactionRequestInterceptor : HandlerInterceptor {

    @Autowired
    private lateinit var multiserviceTransactionContextHolder: MultiserviceTransactionContextHolder

    companion object {
        private val TRANSACTION_HEADER = "X-Transaction"
    }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val transactionId = request.getHeader(TRANSACTION_HEADER) ?: UUID.randomUUID().toString()
        multiserviceTransactionContextHolder.setCurrentTransactionId(transactionId)
        return super.preHandle(request, response, handler)
    }
}
