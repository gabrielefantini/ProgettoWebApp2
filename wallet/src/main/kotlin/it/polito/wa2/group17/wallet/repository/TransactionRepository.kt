package it.polito.wa2.group17.wallet.repository

import it.polito.wa2.group17.wallet.entity.TransactionEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionRepository : CrudRepository<TransactionEntity, Long>
