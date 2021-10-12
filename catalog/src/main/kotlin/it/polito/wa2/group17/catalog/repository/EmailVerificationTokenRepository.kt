package it.polito.wa2.group17.catalog.repository

import it.polito.wa2.group17.catalog.domain.EmailVerificationToken
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.persistence.criteria.Path
import javax.persistence.criteria.Root

@Repository
interface EmailVerificationTokenRepository : CrudRepository<EmailVerificationToken, UUID>,
    TokenExpirationAwareRepository

interface TokenExpirationAwareRepository {
    fun findExpiredTokens(): MutableList<EmailVerificationToken>
}

private class TokenExpirationAwareRepositoryImpl : TokenExpirationAwareRepository {
    @PersistenceContext
    private lateinit var entityManager: EntityManager

    override fun findExpiredTokens(): MutableList<EmailVerificationToken> {
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteriaQuery = criteriaBuilder.createQuery(EmailVerificationToken::class.java)

        val token: Root<EmailVerificationToken> = criteriaQuery.from(EmailVerificationToken::class.java)
        val expireDatePath: Path<Instant> = token.get("expireDate")

        criteriaQuery.select(token).where(criteriaBuilder.lessThan(expireDatePath, Instant.now()))
        return entityManager.createQuery(criteriaQuery).resultList
    }

}
