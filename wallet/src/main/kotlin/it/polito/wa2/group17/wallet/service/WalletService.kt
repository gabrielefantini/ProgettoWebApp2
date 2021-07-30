package it.polito.wa2.group17.wallet.service

import it.polito.wa2.group17.common.exception.EntityNotFoundException
import it.polito.wa2.group17.common.exception.GenericBadRequestException
import it.polito.wa2.group17.common.transaction.MultiserviceTransactional
import it.polito.wa2.group17.common.transaction.Rollback
import it.polito.wa2.group17.common.utils.converter.convert
import it.polito.wa2.group17.wallet.connector.UsersConnector
import it.polito.wa2.group17.wallet.entity.TransactionEntity
import it.polito.wa2.group17.wallet.entity.WalletEntity
import it.polito.wa2.group17.wallet.exception.InvalidTransactionException
import it.polito.wa2.group17.wallet.model.Transaction
import it.polito.wa2.group17.wallet.model.Wallet
import it.polito.wa2.group17.wallet.repository.TransactionRepository
import it.polito.wa2.group17.wallet.repository.WalletRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant

interface WalletService {

    @Throws(EntityNotFoundException::class)
    fun getWallet(walletId: Long): Wallet

    @Throws(EntityNotFoundException::class)
    fun addWalletToUser(userId: Long): Wallet

    @Throws(EntityNotFoundException::class, InvalidTransactionException::class)
    fun performTransaction(
        amount: Double,
        reason: String,
        sourceWalletId: Long,
        userId: Long,
        timeInstant: Instant = Instant.now()
    ): Transaction

    @Throws(EntityNotFoundException::class)
    fun getTransactionsOfWallet(walletId: Long): List<Transaction>

    @Throws(EntityNotFoundException::class)
    fun getTransactionsOfWallet(walletId: Long, from: Instant, to: Instant = Instant.now()): List<Transaction>

    @Throws(EntityNotFoundException::class)
    fun getTransactionOfWallet(walletId: Long, transactionId: Long): Transaction

    @Throws(EntityNotFoundException::class)
    fun getWalletFromUser(userId: Long): Wallet

}


@Service
private open class WalletServiceImpl(
    val walletRepository: WalletRepository,
    val transactionRepository: TransactionRepository,
    val usersConnector: UsersConnector
) : WalletService {

    companion object {
        private val logger = LoggerFactory.getLogger(WalletServiceImpl::class.java)
    }

    override fun getWallet(walletId: Long): Wallet {
        logger.info("Searching for wallet with id {}", walletId)
        val wallet = walletRepository.findByIdOrNull(walletId)
        if (wallet != null) {
            return wallet.convert()
        }
        throw EntityNotFoundException("Wallet with id $walletId")
    }

    @MultiserviceTransactional
    override fun addWalletToUser(userId: Long): Wallet {
        logger.info("Adding new wallet to user with id {}", userId)
        val wallet = WalletEntity(userId = userId)
        walletRepository.save(wallet)
        logger.info("Wallet added.")
        return wallet.convert()
    }

    @Rollback
    private fun rollbackForAddWalletToUser(userId: Long, createdWallet: Wallet) {
        logger.warn("Removing wallet {} from user with id {}", createdWallet.id, userId)
        walletRepository.deleteById(createdWallet.id)
        logger.info("Wallet {} removed from user with id {}", createdWallet.id, userId)
    }

    @MultiserviceTransactional
    override fun performTransaction(
        amount: Double,
        reason: String,
        sourceWalletId: Long,
        userId: Long,
        timeInstant: Instant
    ): Transaction {

        logger.info(
            "Performing transaction of {} from wallet {}",
            amount,
            sourceWalletId,
        )

        if (amount == 0.0)
            throw InvalidTransactionException(
                sourceWalletId, amount,
                "Illegal transaction with no amount"
            )

        val sourceWallet = walletRepository.findByIdOrNull(sourceWalletId)
            ?: throw EntityNotFoundException("Source wallet with id $sourceWalletId")

        if (amount > 0) {
            //check if an admin is performing this operation
            if (!usersConnector.isAdmin(userId))
                throw InvalidTransactionException(
                    sourceWalletId,
                    amount,
                    "Transactions with positive amount can be performed only by admins"
                )
        } else {
            // check if the authenticated user is the owner of the wallet
            if (sourceWallet.userId != userId)
                throw InvalidTransactionException(sourceWalletId, amount, "User $userId is not the owner of the wallet")
        }
        //check if source wallet has enough amount
        if (sourceWallet.amount + amount < 0) {
            throw InvalidTransactionException(
                sourceWalletId,
                amount,
                "Source wallet has not enough credits"
            )
        }

        val transaction = TransactionEntity(
            amount = amount,
            source = sourceWallet, reason = reason, timeInstant = timeInstant
        )
        transactionRepository.save(transaction)
        sourceWallet.amount += amount
        walletRepository.save(sourceWallet)
        logger.info("Transaction performed.")
        return transaction.convert()

    }

    @Rollback
    private fun rollbackForPerformTransaction(
        amount: Double,
        reason: String,
        sourceWalletId: Long,
        userId: Long,
        timeInstant: Instant,
        transaction: Transaction,
    ) {

        logger.warn(
            "Performing rollback of transaction {} from wallet {}",
            transaction.id,
            sourceWalletId,
        )

        val sourceWallet = walletRepository.findByIdOrNull(sourceWalletId)
        if (sourceWallet == null) {
            logger.error(
                "Cannot rollback transaction ${transaction.id} " +
                        "because wallet $sourceWalletId does not exist any more!"
            )
            transactionRepository.deleteById(transaction.id)
            return
        }

        if (transactionRepository.findById(transaction.id).isPresent) {
            transactionRepository.deleteById(transaction.id)
            sourceWallet.amount -= amount
            walletRepository.save(sourceWallet)
        }
        logger.info("Transaction {} rolled back.", transaction.id)
    }

    override fun getTransactionsOfWallet(walletId: Long): List<Transaction> {
        logger.info("Retrieving transactions of wallet {}", walletId)
        logger.info("Searching for wallet with id {}", walletId)
        val wallet = walletRepository.findByIdOrNull(walletId)
        if (wallet != null) {
            return wallet.transactions.map { it.convert() }
        }
        throw EntityNotFoundException("Wallet with id $walletId")
    }

    override fun getTransactionsOfWallet(walletId: Long, from: Instant, to: Instant): List<Transaction> {
        if (!to.isAfter(from))
            throw GenericBadRequestException("Invalid date range from $from to $to")
        logger.info("Retrieving transactions of wallet {} from {} to {}", walletId, from, to)
        val list = getTransactionsOfWallet(walletId)
        return list.filter { it.timeInstant.isAfter(from) && it.timeInstant.isBefore(to) }
    }

    override fun getTransactionOfWallet(walletId: Long, transactionId: Long): Transaction {
        logger.info("Retrieving transaction {} from wallet ", transactionId, walletId)
        return transactionRepository.findById(transactionId)
            .orElseThrow { EntityNotFoundException(transactionId) }
            .apply {
                if (source.getId() != walletId)
                    throw EntityNotFoundException("$transactionId of wallet $walletId")
            }
            .convert()
    }

    override fun getWalletFromUser(userId: Long): Wallet {
        logger.info("Retrieving wallet from user {}",userId);
        return walletRepository.findByUserId(userId)?.let {
            convert()
        }?: run{
            throw EntityNotFoundException(userId)
        }
    }

}
