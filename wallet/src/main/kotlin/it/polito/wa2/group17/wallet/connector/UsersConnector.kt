package it.polito.wa2.group17.wallet.connector

import it.polito.wa2.group17.common.connector.RemoteCaller
import it.polito.wa2.group17.wallet.dto.UserDto
import it.polito.wa2.group17.wallet.enums.Role
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class UsersConnector {

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Value("\${connectors.users}")
    private lateinit var uri: String

    @RemoteCaller
    fun isAdmin(userId: Long): Boolean {
        return Role.ADMIN == restTemplate
            .getForEntity("$uri/{}", UserDto::class.java, userId)
            .body?.role
    }

    @RemoteCaller
    fun isCustomer(userId: Long): Boolean {
        return Role.CUSTOMER == restTemplate
            .getForEntity("$uri/{}", UserDto::class.java, userId)
            .body?.role
    }

}
