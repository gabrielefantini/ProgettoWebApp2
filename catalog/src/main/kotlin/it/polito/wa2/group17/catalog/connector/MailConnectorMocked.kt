package it.polito.wa2.group17.catalog.connector

import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.common.mail.MailConnector
import it.polito.wa2.group17.common.mail.MailRequestDto
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Primary
/*
@Connector
@ConditionalOnProperty(prefix = "connectors.mail.mock", name = ["enabled"], havingValue = "true")
class MailConnectorMocked: MailConnector() {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun sendMail(mailRequestDto: MailRequestDto) = logger.info("Mail sent to ${mailRequestDto.destination}")
}
*/