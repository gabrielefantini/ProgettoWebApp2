package it.polito.wa2.group17.wallet.service

import it.polito.wa2.group17.common.exception.EntityNotFoundException
import it.polito.wa2.group17.common.exception.GenericBadRequestException
import it.polito.wa2.group17.common.transaction.MultiserviceTransactional
import it.polito.wa2.group17.common.transaction.RollbackFor
import it.polito.wa2.group17.common.utils.converter.convert
import it.polito.wa2.group17.exceptions.security.UserNotAllowedException
import it.polito.wa2.group17.wallet.entity.TransactionEntity
import it.polito.wa2.group17.wallet.entity.WalletEntity
import it.polito.wa2.group17.wallet.exception.InvalidTransactionException
import it.polito.wa2.group17.wallet.model.Transaction
import it.polito.wa2.group17.wallet.model.Wallet
import it.polito.wa2.group17.wallet.repository.TransactionRepository
import it.polito.wa2.group17.wallet.repository.UserRepository
import it.polito.wa2.group17.wallet.repository.WalletRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.time.Instant

interface WalletService {

    @Throws(EntityNotFoundException::class)
    fun getWallet(walletId: Long): Wallet?

    @Throws(EntityNotFoundException::class)
    fun addWalletToUser(userId: Long): Wallet

    @Throws(EntityNotFoundException::class, InvalidTransactionException::class)
    fun performTransaction(sourceWalletId: Long, destinationWalletId: Long, amount: Double): Transaction

    @Throws(EntityNotFoundException::class)
    fun getTransactionsOfWallet(walletId: Long): List<Transaction>

    @Throws(EntityNotFoundException::class)
    fun getTransactionsOfWallet(walletId: Long, from: Instant, to: Instant = Instant.now()): List<Transaction>

}


@Service
private class WalletServiceImpl(
    val walletRepository: WalletRepository,
    val userRepository: UserRepository,
    val transactionRepository: TransactionRepository
) : WalletService {

    companion object {
        private val logger = LoggerFactory.getLogger(WalletServiceImpl::class.java)

        private const val ADD_WALLET_TRANSACTION_ID = "addWallet"

    }

    override fun getWallet(walletId: Long): Wallet {
        logger.info("Searching for wallet with id {}", walletId)
        val wallet = walletRepository.findByIdOrNull(walletId)
        if (wallet != null) {
            return wallet.convert()
        }
        throw EntityNotFoundException("Wallet with id $walletId")
    }

    @MultiserviceTransactional(ADD_WALLET_TRANSACTION_ID)
    override fun addWalletToUser(userId: Long): Wallet {
        logger.info("Adding new wallet to user with id {}", userId)
        val user = userRepository.findByIdOrNull(userId)
        if (user != null) {
            val wallet = WalletEntity(user = user)
            walletRepository.save(wallet)
            logger.info("Wallet added.")
            return wallet.convert()
        }
        throw EntityNotFoundException("user with id $userId")
    }

    @RollbackFor(ADD_WALLET_TRANSACTION_ID)
    private fun removeWalletFromUser(userId: Long, createdWallet: Wallet) {
        logger.info("Removing wallet from user with id {}", userId)
        val user = userRepository.findByIdOrNull(userId)
        if (user != null) {
            val wallet = WalletEntity(user = user)
            walletRepository.save(wallet)
            logger.info("Wallet added.")
            return wallet.convert()
        }
        throw EntityNotFoundException("user with id $userId")
    }

    override fun performTransaction(sourceWalletId: Long, destinationWalletId: Long, amount: Double): Transaction {
        logger.info(
            "Performing transaction of {} from wallet {} to wallet {}",
            amount,
            sourceWalletId,
            destinationWalletId
        )

        if (amount <= 0) {
            throw InvalidTransactionException(sourceWalletId, destinationWalletId, amount, "Amount is <=0")
        }
        if (sourceWalletId == destinationWalletId) {
            throw InvalidTransactionException(
                sourceWalletId,
                destinationWalletId,
                amount,
                "Cannot perform transactions with same destination as source"
            )
        }

        //check if both wallet exists
        val sourceWallet = walletRepository.findByIdOrNull(sourceWalletId)
        val destinationWallet = walletRepository.findByIdOrNull(destinationWalletId)
        if (sourceWallet != null && destinationWallet != null) {

            // check if the authenticated user is the owner of the wallet
            val userDetails = SecurityContextHolder.getContext().authentication.name
            if (sourceWallet.user.userEntity.username != userDetails)
                throw UserNotAllowedException(userDetails)

            //both wallets are available
            //check if source wallet has enough amount
            if (sourceWallet.amount < amount) {
                throw InvalidTransactionException(
                    sourceWalletId,
                    destinationWalletId,
                    amount,
                    "Source wallet has not enough credits"
                )
            }

            val transaction = TransactionEntity(amount = amount, source = sourceWallet, dest = destinationWallet)
            transactionRepository.save(transaction)
            //update both wallets amount
            sourceWallet.amount -= amount
            destinationWallet.amount += amount
            walletRepository.saveAll(listOf(sourceWallet, destinationWallet))
            logger.info("Transaction performed.")
            return transaction.convert()
        }
        if (sourceWallet == null) {
            throw EntityNotFoundException("Source wallet with id $sourceWalletId")
        } else {
            throw EntityNotFoundException("Destination wallet with id $destinationWalletId")
        }

    }

    override fun getTransactionsOfWallet(walletId: Long): List<Transaction> {
        logger.info("Retrieving transactions of wallet {}", walletId)
        logger.info("Searching for wallet with id {}", walletId)
        val wallet = walletRepository.findByIdOrNull(walletId)
        if (wallet != null) {
            val transactions = mutableListOf<Transaction>()
            transactions.addAll(wallet.incomingTransactions.map { it.convert() })
            transactions.addAll(wallet.outgoingTransactions.map { it.convert() })
            return transactions
        }
        throw EntityNotFoundException("Wallet with id $walletId")
    }

    override fun getTransactionsOfWallet(walletId: Long, from: Instant, to: Instant): List<Transaction> {
        if (!to.isAfter(from)) throw GenericBadRequestException("Invalid date range from $from to $to")
        logger.info("Retrieving transactions of wallet {} from {} to {}", walletId, from, to)
        val list = getTransactionsOfWallet(walletId)
        return list.filter { it.timeInstant.isAfter(from) && it.timeInstant.isBefore(to) }
    }

}
