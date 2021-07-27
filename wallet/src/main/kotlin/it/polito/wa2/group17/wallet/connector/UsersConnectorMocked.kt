package it.polito.wa2.group17.wallet.connector

import it.polito.wa2.group17.common.connector.Connector
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Primary
import java.util.*

@Connector
@Primary
@ConditionalOnProperty(prefix = "connectors.users.mock", name = ["enabled"], havingValue = "true")
class UsersConnectorMocked : UsersConnector() {

    private val random = Random()

    override fun isAdmin(userId: Long): Boolean = random.nextBoolean()

    override fun isCustomer(userId: Long): Boolean = random.nextBoolean()

}
