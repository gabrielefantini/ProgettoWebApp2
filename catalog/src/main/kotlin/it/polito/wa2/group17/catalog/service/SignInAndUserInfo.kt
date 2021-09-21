package it.polito.wa2.group17.catalog.service

import it.polito.wa2.group17.catalog.dto.UserDetailsDto
import it.polito.wa2.group17.catalog.model.BadLoginResponse
import it.polito.wa2.group17.catalog.model.LoginRequest
import it.polito.wa2.group17.catalog.model.LoginResponse
import it.polito.wa2.group17.catalog.repository.UserRepository
import it.polito.wa2.group17.catalog.security.jwt.JwtUtils
import it.polito.wa2.group17.common.transaction.MultiserviceTransactional
import it.polito.wa2.group17.common.transaction.Rollback
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import springfox.documentation.spi.service.contexts.SecurityContext

interface SignInAndUserInfo {
    @Throws(it.polito.wa2.group17.common.exception.EntityNotFoundException::class)
    fun updateUserInformation(password: String, name: String, surname: String, deliveryAddr:String): UserDetailsDto

    fun signInUser(req: LoginRequest): ResponseEntity<*>
}


@Service
@Transactional
class SignInAndUserInfoImpl(private val userRepository: UserRepository) : SignInAndUserInfo {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var authManager: AuthenticationManager

    @Autowired
    private lateinit var encoder: PasswordEncoder

    @Autowired
    private lateinit var jwtUtils: JwtUtils

    @MultiserviceTransactional
    override fun updateUserInformation(password: String, name: String, surname: String, deliveryAddr:String): UserDetailsDto {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByUsername(username).get()
        val userDetailsDto = UserDetailsDto(user.getId(), user.username, user.password, user.email, user.isEnabled, user.getRoleNames(), user.name, user.surname, user.deliveryAddr)
        userRepository.updateUserInformation(username, encoder.encode(password), name, surname, deliveryAddr)
        return userDetailsDto
    }

    override fun signInUser(req: LoginRequest): ResponseEntity<*> {
        return try {
            val authentication = authManager.authenticate(
                UsernamePasswordAuthenticationToken(req.username, req.password)
            )
            logger.info("User {} logged in", req.username)
            SecurityContextHolder.getContext().authentication = authentication

            val userDetails = authentication.principal as UserDetailsDto

            val jwt = jwtUtils.generateJwtToken(authentication)
            val roles = userDetails.authorities.map { it.authority }.toList()
            ResponseEntity.ok(
                LoginResponse(
                    jwt,
                    userDetails.id!!,
                    userDetails.username,
                    userDetails.email,
                    roles
                )
            )
        } catch (e: Exception) {
            logger.error("Log in failed.")
            logger.error(e.toString())
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(BadLoginResponse("Log in failed. Please try again."))
        }
    }

    @Rollback
    private fun rollbackForUpdateUserInformation(password: String, name: String, surname: String, deliveryAddr:String, userDet: UserDetailsDto) {
        val user = userRepository.findById(userDet.id!!).get()
        userRepository.updateUserInformation(user.username, user.password, userDet.name, userDet.surname, userDet.deliveryAddr)
    }
}
