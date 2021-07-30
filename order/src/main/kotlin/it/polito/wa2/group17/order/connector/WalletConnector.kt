package it.polito.wa2.group17.order.connector

import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.order.model.TransactionModel
import it.polito.wa2.group17.order.model.WalletModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.client.RestTemplate

@Connector
class WalletConnector {
    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Value("\${connectors.wallet.uri}")
    private lateinit var uri: String

    fun getUserWallet(userId: Long): WalletModel? {
        return restTemplate
            .getForEntity("$uri/wallets/{}", WalletModel::class.java, userId)
            .body
    }

    fun addWalletTransaction(transaction: TransactionModel, walletId: Long): TransactionModel? {
        return restTemplate
            .postForEntity("$uri/wallets/{}/transactions",transaction, TransactionModel::class.java, walletId)
            .body
    }
}