package it.polito.wa2.group17.catalog.connector

import it.polito.wa2.group17.catalog.dto.UserDetailsDto
import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.common.dto.OrderDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.client.RestTemplate

@Connector
class LoginConnector {

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Value("\${connectors.login.uri}")
    private lateinit var uri: String

    fun findByUsername(username: String): UserDetailsDto? {
        return restTemplate.getForEntity(
            "$uri/findByUsername/$username", UserDetailsDto::class.java
        ).body
    }
}
