package it.polito.wa2.group17.common.transaction

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class MultiserviceTransactionRequestInterceptor : HandlerInterceptor, ClientHttpRequestInterceptor {

    @Autowired
    private lateinit var multiserviceTransactionContextHolder: MultiserviceTransactionContextHolder

    companion object {
        const val TRANSACTION_HEADER = "X-Transaction"
    }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val transactionId = request.getHeader(TRANSACTION_HEADER) ?: UUID.randomUUID().toString()
        multiserviceTransactionContextHolder.setCurrentTransactionId(transactionId)
        return super.preHandle(request, response, handler)
    }

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        request.headers.add(TRANSACTION_HEADER, MultiserviceTransactionContextHolder.getCurrentTransactionID())
        return execution.execute(request, body)
    }
}

@Configuration
private open class MultiserviceTransactionRequestInterceptorConfiguration : WebMvcConfigurer {
    @Autowired
    private lateinit var multiserviceTransactionRequestInterceptor: MultiserviceTransactionRequestInterceptor
    override fun addInterceptors(registry: InterceptorRegistry) {
        super.addInterceptors(registry)
        registry.addInterceptor(multiserviceTransactionRequestInterceptor)
    }
}
