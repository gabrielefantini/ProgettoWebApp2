package it.polito.wa2.group17.warehouse.connector

import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.warehouse.dto.UserDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.client.RestTemplate

@Connector
class UsersConnector {

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Value("\${connectors.login.uri}")
    private lateinit var uri: String

    fun getAdmins(): List<UserDto> {
        return restTemplate.getForEntity(
            "$uri/auth/admins", Array<UserDto>::class.java
        ).body?.toList() ?: listOf()
    }
}
