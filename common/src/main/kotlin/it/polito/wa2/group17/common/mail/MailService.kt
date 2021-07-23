package it.polito.wa2.group17.common.mail

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import org.springframework.util.FileCopyUtils
import java.io.InputStreamReader
import javax.annotation.PostConstruct

interface MailService {
    fun sendMessage(destination: String, subject: String, body: String)
}

@Service
private class MailServiceImpl : MailService {
    @Autowired
    private lateinit var javaMailSender: JavaMailSender
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${spring.mail.username}")
    private lateinit var source: String

    @Value("\${spring.mail.template.textPlaceholder}")
    private lateinit var textPlaceholder: String

    @Value("classpath:mail-template.html")
    private lateinit var mailTemplateResource: Resource

    private lateinit var basicMailBody: String

    @PostConstruct
    private fun initMailTemplate() {
        basicMailBody = FileCopyUtils.copyToString(InputStreamReader(mailTemplateResource.inputStream))
    }

    override fun sendMessage(destination: String, subject: String, body: String) {
        logger.info("Sending email with subject '{}' to {}", subject, destination)
        javaMailSender.send(SimpleMailMessage().apply {
            setFrom(source)
            setTo(destination)
            setSubject(subject)
            setText(createMailBody(body))
        })
        logger.info("Email '{}' successfully sent to {}.", subject, destination)
    }

    private fun createMailBody(body: String): String = basicMailBody.replace(textPlaceholder, body)

}
