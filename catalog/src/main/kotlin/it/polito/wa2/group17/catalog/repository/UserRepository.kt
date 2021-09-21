package it.polito.wa2.group17.catalog.repository

import it.polito.wa2.group17.catalog.domain.User
import it.polito.wa2.group17.catalog.dto.UserDetailsDto
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface UserRepository : CrudRepository<User, Long> {

    fun findByUsername(username: String): Optional<User>
    fun findByEmail(email: String): Optional<User>

    @Modifying
    @Transactional
    @Query ("UPDATE User set password = :password, name = :name, surname = :surname, deliveryAddr = :deliveryAddr where username = :username")
    fun updateUserInformation(username: String, password: String, name: String, surname: String, deliveryAddr:String)

    @Query ("SELECT u from User u where roles IN :possibilities")
    fun findAdmin(possibilities: List<String>): List<User>

    fun deleteUserByEmail(email: String)

}
