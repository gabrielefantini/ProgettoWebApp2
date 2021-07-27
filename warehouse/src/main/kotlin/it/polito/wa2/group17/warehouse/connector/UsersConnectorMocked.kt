package it.polito.wa2.group17.warehouse.connector

import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.warehouse.dto.UserDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Primary
import java.util.*

@Connector
@Primary
@ConditionalOnProperty(prefix = "connectors.users", name = ["mocked"], havingValue = "true")
class UsersConnectorMocked : UsersConnector() {

    @Autowired
    private lateinit var usersMockedProperties: UsersMockedProperties

    private val random = Random()

    override fun getAdmins(): List<UserDto> =
        usersMockedProperties.testEmails.map { UserDto().apply { email = it; id = random.nextLong() } }
}

@ConfigurationProperties("connectors.users.mocked")
class UsersMockedProperties {
    var testEmails = mutableListOf<String>()
}
