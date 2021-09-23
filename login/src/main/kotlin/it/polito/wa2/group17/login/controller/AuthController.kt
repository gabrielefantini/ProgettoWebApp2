package it.polito.wa2.group17.login.controller

import it.polito.wa2.group17.login.dto.UserDetailsDto
import it.polito.wa2.group17.login.model.LoginRequest
import it.polito.wa2.group17.login.model.UserRegistration
import it.polito.wa2.group17.login.security.OnlyEnabledUsers
import it.polito.wa2.group17.login.security.jwt.JwtUtils
import it.polito.wa2.group17.login.service.SignInAndUserInfo
import it.polito.wa2.group17.login.service.UserDetailsServiceExtended
import it.polito.wa2.group17.common.utils.extractErrors
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
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

    @Autowired
    private lateinit var signInAndUserInfo: SignInAndUserInfo

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
        return signInAndUserInfo.signInUser(req)
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

    @PostMapping("/updateUserInfo")
    @OnlyEnabledUsers
    fun updateUserInfo(@RequestParam password: String, @RequestParam name: String, @RequestParam surname: String, @RequestParam deliveryAddr:String): ResponseEntity<*> {
        return ResponseEntity.ok(signInAndUserInfo.updateUserInformation(password, name, surname, deliveryAddr))
    }

    @GetMapping("/getUserInfo")
    @OnlyEnabledUsers
    fun getMyInformation(): ResponseEntity<UserDetailsDto>{
        return ResponseEntity.ok(signInAndUserInfo.getUserInformation())
    }

    @GetMapping("/findByUsername/{username}")
    fun findUserByUsername(@PathVariable username:String):ResponseEntity<UserDetailsDto>? {
        val user = signInAndUserInfo.findUser(username)
        return if (user != null) ResponseEntity.ok(UserDetailsDto(user.id, user.username, "Password not available!", user.email, user.isEnabled, user.roles, user.name, user.surname, user.deliveryAddr))
        else return null
    }
}
