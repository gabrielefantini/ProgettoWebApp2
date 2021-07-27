package it.polito.wa2.group17.wallet.connector

import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.wallet.dto.UserDto
import it.polito.wa2.group17.wallet.enums.Role
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.client.RestTemplate

@Connector
class UsersConnector {

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Value("\${connectors.users.uri}")
    private lateinit var uri: String

    fun isAdmin(userId: Long): Boolean {
        return Role.ADMIN == restTemplate
            .getForEntity("$uri/{}", UserDto::class.java, userId)
            .body?.role
    }

    fun isCustomer(userId: Long): Boolean {
        return Role.CUSTOMER == restTemplate
            .getForEntity("$uri/{}", UserDto::class.java, userId)
            .body?.role
    }

}
