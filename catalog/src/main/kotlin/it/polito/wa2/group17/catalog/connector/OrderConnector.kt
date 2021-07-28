package it.polito.wa2.group17.catalog.connector

import it.polito.wa2.group17.common.connector.Connector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.client.RestTemplate

@Connector
class OrderConnector {

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Value("\${connectors.orders}")
    private lateinit var uri: String

}
