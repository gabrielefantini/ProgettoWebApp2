package it.polito.wa2.group17.wallet.repository

import it.polito.wa2.group17.wallet.entity.UserEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : CrudRepository<UserEntity, Long>
