package it.polito.wa2.group17.catalog.connector

import it.polito.wa2.group17.catalog.dto.UserDetailsDto
import it.polito.wa2.group17.common.connector.Connector
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Primary

@Connector
@Primary
@ConditionalOnProperty(prefix = "connectors.mail.mock", name = ["enabled"], havingValue = "true")
class LoginConnectorMocked: LoginConnector() {

    override fun findByUsername(username: String) = UserDetailsDto(1, "username", "password", "email", true, setOf(), "name", "surname", "addr")
}
