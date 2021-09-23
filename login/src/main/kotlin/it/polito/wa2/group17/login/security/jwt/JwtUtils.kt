package it.polito.wa2.group17.login.security.jwt

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import it.polito.wa2.group17.login.dto.UserDetailsDto
import it.polito.wa2.group17.login.service.UserDetailsServiceExtendedImpl
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.security.Key
import java.security.SignatureException
import java.util.*

@Component
class JwtUtils() {

    private companion object val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var userService: UserDetailsServiceExtendedImpl

    @Value("\${application.jwt.jwtSecret}")
    private lateinit var jwtSecret: String

    @Value("\${application.jwt.jwtExpirationMs}")
    private lateinit var jwtExpirationMs: String

    private fun getSigningKey(): Key {
        return Keys.hmacShaKeyFor(Base64.getEncoder().encode(jwtSecret.encodeToByteArray()))
    }

    fun generateJwtToken(authentication: Authentication): String {
        return Jwts
            .builder()
            .setSubject((authentication.principal as UserDetailsDto).username)
            .setIssuedAt(Date(Date().time))
            .setExpiration(Date((Date().time + 30000)))
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact()
    }

    fun validateJwtToken(authToken: String): Boolean {
        try {
            Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken)
            return true
        } catch (e: SignatureException) {
            logger.error("Invalid JWT signature: {}", e.message)
        } catch (e: MalformedJwtException) {
            logger.error("Invalid JWT token: {}", e.message)
        } catch (e: ExpiredJwtException) {
            logger.error("JWT token is expired: {}", e.message)
        } catch (e: UnsupportedJwtException) {
            logger.error("JWT token is unsupported: {}", e.message)
        } catch (e: IllegalArgumentException) {
            logger.error("JWT claims string is empty: {}", e.message)
        }
        return false
    }

    fun getDetailsFromJwtToken(authToken: String): UserDetailsDto {
        return userService.loadUserByUsername(
            Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken)
                .body
                .subject
        )
    }
}
