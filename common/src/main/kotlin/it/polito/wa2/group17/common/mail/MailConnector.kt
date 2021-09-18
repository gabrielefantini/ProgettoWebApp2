package it.polito.wa2.group17.common.mail

import it.polito.wa2.group17.common.connector.Connector
import org.slf4j.LoggerFactory
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

    private val logger = LoggerFactory.getLogger(javaClass)

    fun sendMail(mailRequestDto: MailRequestDto) {
        logger.info("Inside sendMail")
        logger.info("Uri: {}", uri)
        var targetUri = if (uri.startsWith("http")) uri else "http://$uri"

        logger.info(targetUri)
        restTemplate.postForEntity<Any>(
            "$targetUri/mail", mailRequestDto
        )
    }
}
