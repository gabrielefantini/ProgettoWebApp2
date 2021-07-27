package it.polito.wa2.group17.common.mail

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

interface MailService {
    fun sendMessage(destination: String, subject: String, body: String)
}

@Service
private class MailServiceImpl : MailService {
    @Autowired
    private lateinit var mailConnector: MailConnector
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun sendMessage(destination: String, subject: String, body: String) {
        logger.info("Sending email with subject '{}' to {}", subject, destination)
        mailConnector.sendMail(MailRequestDto(destination, subject, body))
        logger.info("Email '{}' successfully sent to {}.", subject, destination)
    }

}
