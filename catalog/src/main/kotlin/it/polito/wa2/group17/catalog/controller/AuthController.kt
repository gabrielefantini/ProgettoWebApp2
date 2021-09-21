package it.polito.wa2.group17.catalog.controller

import it.polito.wa2.group17.catalog.dto.UserDetailsDto
import it.polito.wa2.group17.catalog.model.BadLoginResponse
import it.polito.wa2.group17.catalog.model.LoginRequest
import it.polito.wa2.group17.catalog.model.LoginResponse
import it.polito.wa2.group17.catalog.model.UserRegistration
import it.polito.wa2.group17.catalog.security.OnlyEnabledUsers
import it.polito.wa2.group17.catalog.security.RoleName
import it.polito.wa2.group17.catalog.security.jwt.JwtUtils
import it.polito.wa2.group17.catalog.service.CatalogService
import it.polito.wa2.group17.catalog.service.UserDetailsServiceExtended
import it.polito.wa2.group17.common.utils.extractErrors
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping(
    value = ["/auth"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class AuthController {

    @Autowired
    private lateinit var userServiceExtended: UserDetailsServiceExtended
    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var authManager: AuthenticationManager

    @Autowired
    private lateinit var jwtUtils: JwtUtils

    @Autowired
    private lateinit var encoder: PasswordEncoder

    @Autowired
    private lateinit var userDetails: UserDetailsServiceExtended

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody userRegistration: UserRegistration,
        bindingResult: BindingResult
    ): ResponseEntity<*> {
        if (bindingResult.hasErrors())
            return ResponseEntity.badRequest().body(bindingResult.extractErrors())
        logger.info("Received user registration request from {}", userRegistration.email)
        userServiceExtended.createUser(
            userRegistration.username,
            encoder.encode(userRegistration.password),
            userRegistration.email,
            userRegistration.name,
            userRegistration.surname,
            userRegistration.address
        )
        userServiceExtended.addRoleToUser(userRegistration.username, RoleName.CUSTOMER.name)
        userServiceExtended.setUserEnabled(userRegistration.username, false)
        return ResponseEntity.ok().build<Any>()
    }

    @GetMapping("/register/renewToken")
    fun renewToken(
        @RequestParam username: String
    ): ResponseEntity<*> {
        logger.info("Received token renew request from {}", username)
        userServiceExtended.createTokenForUser(username)
        return ResponseEntity.ok().build<Any>()
    }

    @PostMapping("/signin")
    fun signIn(
        @Valid @RequestBody req: LoginRequest
    ): ResponseEntity<*> {
        logger.info("User {} is trying to log in", req.username)
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

    @PutMapping("/setAdmin")
    fun setAdmin(@RequestParam username: String, @RequestParam value: Boolean): ResponseEntity<Long>{
        print("Inside setAdmin")
        return ResponseEntity.ok(userDetails.setUserAsAdmin(username, value)?.id)
    }

    @GetMapping("/registrationConfirm")
    fun registrationConfirm(@RequestParam("token") token: String): ResponseEntity<String> {
        logger.info("Received token confirmation request for {}", token)
        userServiceExtended.verifyToken(token)
        return ResponseEntity.ok("User account has been confirmed")
    }

    @GetMapping("/admins")
    fun getAdmins(): List<UserDetailsDto> {
        logger.info("Searching for the admins")
        return userServiceExtended.getAdmins()
    }
}
