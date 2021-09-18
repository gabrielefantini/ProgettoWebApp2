package it.polito.wa2.group17.mail.controller

import it.polito.wa2.group17.mail.model.MailRequest
import it.polito.wa2.group17.mail.service.MailService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping(
    value = ["/mail"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class MailController {

    @Autowired
    private lateinit var mailService: MailService

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    fun sendMail(@RequestBody @Valid mailRequest: MailRequest) {
        logger.info("Inside sendMail of MailController")
        mailService.sendMessage(mailRequest.destination, mailRequest.subject, mailRequest.body)
    }
}
