package it.polito.wa2.group17.order.connector

import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.order.model.TransactionModel
import it.polito.wa2.group17.order.model.WalletModel
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Primary

@Connector
@Primary
@ConditionalOnProperty(prefix = "connectors.wallet.mock", name = ["enabled"], havingValue = "true")
class WalletConnectorMocked: WalletConnector() {
    override fun getUserWallet(userId: Long): WalletModel? = WalletModel(1,userId,20.0)
    override fun addWalletTransaction(transaction: TransactionModel, walletId: Long): TransactionModel? = transaction
}