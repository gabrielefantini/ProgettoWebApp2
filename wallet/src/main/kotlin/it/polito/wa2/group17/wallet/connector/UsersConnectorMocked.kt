package it.polito.wa2.group17.wallet.connector

import it.polito.wa2.group17.common.connector.Connector
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Primary
import java.util.*

@Connector
@ConditionalOnProperty(prefix = "connectors.users.mock", name = ["enabled"], havingValue = "true")
class UsersConnectorMocked : UsersConnector() {

    //override fun isAdmin(userId: Long): Boolean = userId < 0

    //override fun isCustomer(userId: Long): Boolean = userId > 0

}
