package it.polito.wa2.group17.wallet.repository

import it.polito.wa2.group17.wallet.entity.WalletEntity
import it.polito.wa2.group17.wallet.model.Wallet
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface WalletRepository : CrudRepository<WalletEntity, Long>{
    fun findByUserId(userId: Long): Optional<WalletEntity>
}
