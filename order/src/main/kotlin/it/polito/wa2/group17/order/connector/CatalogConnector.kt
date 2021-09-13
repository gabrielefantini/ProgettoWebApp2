package it.polito.wa2.group17.order.connector

import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.order.model.UserModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.client.RestTemplate

@Connector
class CatalogConnector {
    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Value("\${connectors.catalog.uri}")
    private lateinit var uri: String

    fun getUserInfo(userId: Long): UserModel? {
        return restTemplate
            .getForEntity("$uri/getUserInfo/{}", UserModel::class.java, userId)
            .body
    }

}