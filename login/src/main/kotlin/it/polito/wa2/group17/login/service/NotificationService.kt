package it.polito.wa2.group17.login.service

import it.polito.wa2.group17.login.domain.EmailVerificationToken
import it.polito.wa2.group17.login.exceptions.InvalidTokenException
import it.polito.wa2.group17.login.repository.EmailVerificationTokenRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

internal interface NotificationService {
    fun createTokenForUser(username: String): EmailVerificationToken

    @Throws(InvalidTokenException::class)
    fun verifyToken(token: String): String
}

@Service
@Transactional
private open class NotificationServiceImpl : NotificationService {

    @Autowired
    private lateinit var tokenRepo: EmailVerificationTokenRepository

    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${notifications.tokenVerification.expirationMargin}")
    private var expirationMarginMillis: Long? = null

    @PostConstruct
    private fun init() {
        logger.info("Initializing expired token cleaning thread with interval {} millis", expirationMarginMillis)
        Executors.newScheduledThreadPool(1)
            .scheduleAtFixedRate(
                this::deleteExpiredTokens,
                0,
                expirationMarginMillis!!,
                TimeUnit.MILLISECONDS
            )
    }

    @Synchronized
    private fun deleteExpiredTokens() {
        logger.info("Cleaning expired tokens..")
        val expired = tokenRepo.findExpiredTokens()
        logger.debug("Expired tokens are {}", expired)
        tokenRepo.deleteAll(expired)
        logger.info("Deleted {} expired tokens", expired.size)
    }


    override fun createTokenForUser(username: String): EmailVerificationToken {
        logger.info("Creating validation token for user {}", username)
        val token = tokenRepo.save(
            EmailVerificationToken(
                username,
                Instant.now().plusMillis(expirationMarginMillis!!)
            )
        )
        logger.info(
            "Validation token for user {} is {} expiring on {}",
            username,
            token.getId().toString(),
            token.expireDate
        )
        return token
    }

    @Synchronized
    override fun verifyToken(token: String): String {
        logger.info("Verifying token {}", token)
        val storedToken = tokenRepo.findById(UUID.fromString(token))
        if (!storedToken.isPresent || !Instant.now().isBefore(storedToken.get().expireDate)
        ) throw InvalidTokenException(token)
        logger.info("Token {} is valid", token)
        val username = storedToken.get().username
        tokenRepo.delete(storedToken.get())
        return username
    }
}
