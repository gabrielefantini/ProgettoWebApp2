package it.polito.wa2.group17.common.mail

import it.polito.wa2.group17.common.connector.Connector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

@Connector
class MailConnector {

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Value("\${connectors.mail.uri}")
    private lateinit var uri: String

    fun sendMail(mailRequestDto: MailRequestDto) {
        restTemplate.postForEntity<Any>(
            "$uri/mail", mailRequestDto
        )
    }
}
