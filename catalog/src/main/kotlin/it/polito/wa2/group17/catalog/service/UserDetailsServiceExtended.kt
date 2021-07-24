package it.polito.wa2.group17.catalog.service

import it.polito.wa2.group17.catalog.domain.EmailVerificationToken
import it.polito.wa2.group17.catalog.domain.User
import it.polito.wa2.group17.catalog.dto.ConvertibleDto.Factory.fromEntity
import it.polito.wa2.group17.catalog.dto.UserDetailsDto
import it.polito.wa2.group17.catalog.exceptions.auth.EmailAlreadyPresentException
import it.polito.wa2.group17.catalog.exceptions.auth.UserAlreadyPresentException
import it.polito.wa2.group17.catalog.exceptions.auth.UserAlreadyVerifiedException
import it.polito.wa2.group17.catalog.repository.UserRepository
import it.polito.wa2.group17.catalog.security.RoleName
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.Inet4Address
import javax.persistence.EntityNotFoundException

interface UserDetailsServiceExtended : UserDetailsService {
    fun createUser(
        username: String, password: String, email: String,
        name: String, surname: String, address: String,
    )

    @Throws(EntityNotFoundException::class, UserAlreadyVerifiedException::class)
    fun createTokenForUser(username: String, email: String? = null)

    @Throws(EntityNotFoundException::class)
    fun addRoleToUser(username: String, role: String)

    @Throws(EntityNotFoundException::class)
    fun setUserEnabled(username: String, enabled: Boolean)

    @PreAuthorize("hasRole('ADMIN')")
    fun enableUser(username: String, enabled: Boolean)

    @Throws(EntityNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetailsDto
    fun verifyToken(token: String)

    @Throws(EntityNotFoundException::class)
    fun getCustomerIdForUser(username: String): Long?
}

@Service
@Transactional
class UserDetailsServiceExtendedImpl(private val userRepository: UserRepository) : UserDetailsServiceExtended {
    @Autowired
    private lateinit var notificationService: NotificationService

    @Autowired
    private lateinit var mailService: MailService


    @Value("\${server.port:8080}")
    private lateinit var localPort: String

    @Value("\${notifications.tokenVerification.subject}")
    private lateinit var tokenMessageSubject: String

    @Value("\${notifications.tokenVerification.body}")
    private lateinit var tokenMessageBody: String

    private val logger = LoggerFactory.getLogger(javaClass)

    private companion object {
        val localAddress: String = Inet4Address.getLocalHost().hostName
    }

    private fun computeTokenEndpoint(token: EmailVerificationToken) =
        "http://$localAddress:$localPort/auth/registrationConfirm?token=${
            token.getId().toString()
        }"

    override fun createUser(
        username: String, password: String, email: String,
        name: String, surname: String, address: String,
    ) {
        if (userRepository.findByUsername(username).isPresent) {
            throw UserAlreadyPresentException(username)
        }
        if (userRepository.findByEmail(email).isPresent) {
            throw EmailAlreadyPresentException(email)
        }
        logger.info("Creating user {}", username)
        val user = userRepository.save(User(username, password, email, name, surname, address, false))
        logger.info("User {} created", username)
        logger.info("Creating customer for user {}", username)
        //val customer = customerRepository.save(Customer(name, surname, address, email, user = user))
        //logger.info("Created customer with id {} for user {}", customer.getId(), username)
        //user.customer = customer
        createTokenForUser(username, email)
    }

    override fun createTokenForUser(username: String, email: String?) {
        val user = userRepository.findByUsername(username).orElseThrow { EntityNotFoundException("username $username") }
        if (user.isEnabled)
            throw UserAlreadyVerifiedException(username)

        val existingEmail = email ?: user.email

        logger.info("Creating token for user $username sending it to $existingEmail")
        val token = notificationService.createTokenForUser(username)
        mailService.sendMessage(
            existingEmail,
            tokenMessageSubject,
            String.format(tokenMessageBody, computeTokenEndpoint(token), token.expireDate)
        )
    }

    override fun loadUserByUsername(username: String): UserDetailsDto {
        val user = userRepository.findByUsername(username)
        return fromEntity(user.orElseThrow { EntityNotFoundException("username $username") })
    }

    override fun verifyToken(token: String) {
        val username = notificationService.verifyToken(token)
        setUserEnabled(username, true)
    }

    override fun getCustomerIdForUser(username: String): Long? =
        userRepository.findByUsername(username).orElseThrow { EntityNotFoundException("username $username") }
            .let {
                if (it.getRoleNames().contains(RoleName.CUSTOMER))
                    it.getId()
                else null
            }


    override fun addRoleToUser(username: String, role: String) {
        logger.info("Adding role {} to {}", role, username)
        userRepository.findByUsername(username)
            .orElseThrow { EntityNotFoundException("username $username") }
            .addRoleName(role)

    }

    @PreAuthorize("hasRole('ADMIN')")
    override fun enableUser(username: String, enabled: Boolean) {
        logger.info("enableUser started")
        userRepository.findByUsername(username)
            .orElseThrow { EntityNotFoundException("username $username") }
            .isEnabled = enabled
    }


    override fun setUserEnabled(username: String, enabled: Boolean) {
        logger.info("{} user {}", if (enabled) "Enabling" else "Disabling", username)
        userRepository.findByUsername(username)
            .orElseThrow { EntityNotFoundException("username $username") }
            .isEnabled = enabled
    }

}
