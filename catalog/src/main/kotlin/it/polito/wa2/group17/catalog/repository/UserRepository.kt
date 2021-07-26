package it.polito.wa2.group17.catalog.repository

import it.polito.wa2.group17.catalog.domain.User
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : CrudRepository<User, Long> {
    fun findByUsername(username: String): Optional<User>
    fun findByEmail(email: String): Optional<User>

    @Query ("UPDATE User set email = :email, name = :name, surname = :surname, deliveryAddr = :deliveryAddr where username = :username")
    fun updateUserInformation(username: String, email: String, name: String, surname: String, deliveryAddr:String): Optional<User>
}
