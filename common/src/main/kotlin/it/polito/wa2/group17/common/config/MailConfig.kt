package it.polito.wa2.group17.common.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

@Configuration
class MailConfig {

    @Value("\${spring.mail.host}")
    private lateinit var hostConfig: String

    @Value("\${spring.mail.port}")
    private var portConfig: Int? = null

    @Value("\${spring.mail.username}")
    private lateinit var usernameConfig: String

    @Value("\${spring.mail.password}")
    private lateinit var passConfig: String

    @Value("\${spring.mail.protocol}")
    private lateinit var protocolConfig: String

    @Value("\${spring.mail.properties.mail.smtp.auth}")
    private var auth: Boolean? = null

    @Value("\${spring.mail.properties.mail.smtp.starttls.enable}")
    private var tls: Boolean? = null

    @Value("\${spring.mail.properties.mail.debug}")
    private var debug: Boolean? = null


    @Bean
    fun getMailSender(): JavaMailSender = JavaMailSenderImpl().apply {
        host = hostConfig
        port = portConfig!!
        username = usernameConfig
        password = passConfig
        javaMailProperties["mail.smtp.auth"] = auth
        javaMailProperties["mail.smtp.starttls.enable"] = tls
        javaMailProperties["mail.debug"] = debug
        javaMailProperties["mail.transport.protocol"] = protocolConfig
    }

}
