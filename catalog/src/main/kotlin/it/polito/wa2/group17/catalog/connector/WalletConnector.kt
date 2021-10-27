package it.polito.wa2.group17.catalog.connector

import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.common.dto.Transaction
import it.polito.wa2.group17.common.dto.TransactionRequest
import it.polito.wa2.group17.common.dto.Wallet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.web.client.RestTemplate

@Connector
@Primary
class WalletConnector {
    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Value("\${connectors.wallet.uri}")
    private lateinit var uri: String


    fun getWalletsByUserId(userId: Long): Wallet? {
        return restTemplate.getForEntity(
            "$uri/wallets/users/$userId", Wallet::class.java
        ).body
    }

    fun addWalletToUser(userId: Long): Wallet? {
        return restTemplate.postForEntity(
            "$uri/wallets",userId,Wallet::class.java
        ).body
    }

    fun performTransaction(userId: Long,walletId:Long,amount: Long): Transaction? {
        val transactionReq = TransactionRequest(
            reason = "Admin generic transaction",
            amount = amount.toDouble(),
            userId = userId
        )
        return restTemplate.postForEntity(
            "$uri/wallets/$walletId/transactions",transactionReq,Transaction::class.java
        ).body
    }

}