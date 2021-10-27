package it.polito.wa2.group17.order.connector

import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.order.model.UserModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.web.client.RestTemplate

@Connector
@Primary
class CatalogConnector {
    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Value("\${connectors.catalog.uri}")
    private lateinit var uri: String

    fun getAdmins(): List<UserModel> {
        return restTemplate.getForEntity(
            "$uri/auth/admins", Array<UserModel>::class.java
        ).body?.toList() ?: listOf()
    }

}