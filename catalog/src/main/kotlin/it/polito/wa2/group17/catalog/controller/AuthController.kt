package it.polito.wa2.group17.catalog.controller

import io.swagger.annotations.ApiOperation
import it.polito.wa2.group17.catalog.dto.UserDetailsDto
import it.polito.wa2.group17.catalog.model.LoginRequest
import it.polito.wa2.group17.catalog.model.UserRegistration
import it.polito.wa2.group17.catalog.security.OnlyAdmins
import it.polito.wa2.group17.catalog.security.OnlyEnabledUsers
import it.polito.wa2.group17.catalog.security.jwt.JwtUtils
import it.polito.wa2.group17.catalog.service.SignInAndUserInfo
import it.polito.wa2.group17.catalog.service.UserDetailsServiceExtended
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
@RequestMapping (
    value = ["/auth"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class AuthController {

    @Autowired
    private lateinit var userServiceExtended: UserDetailsServiceExtended

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var encoder: PasswordEncoder

    @Autowired
    private lateinit var userDetails: UserDetailsServiceExtended

    @Autowired
    private lateinit var signInAndUserInfo: SignInAndUserInfo

    @ApiOperation(value="Register to the application",tags = ["auth-controller"])
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

    @ApiOperation(value="Get another token for registration",tags = ["auth-controller"])
    @GetMapping("/register/renewToken")
    fun renewToken(
        @RequestParam username: String
    ): ResponseEntity<*> {
        logger.info("Received token renew request from {}", username)
        userServiceExtended.createTokenForUser(username)
        return ResponseEntity.ok().build<Any>()
    }

    @ApiOperation(value="Login into the application",tags = ["auth-controller"])
    @PostMapping("/signin")
    fun signIn(
        @Valid @RequestBody req: LoginRequest
    ): ResponseEntity<*> {
        logger.info("User {} is trying to log in", req.username)
        return signInAndUserInfo.signInUser(req)
    }

    @ApiOperation(value="Give a given user the admin role",tags = ["auth-controller"])
    @PutMapping("/setAdmin")
    fun setAdmin(@RequestParam username: String, @RequestParam value: Boolean): ResponseEntity<Long>{
        print("Inside setAdmin")
        return ResponseEntity.ok(userDetails.setUserAsAdmin(username, value)?.id)
    }

    @ApiOperation(value="Confirm your registration with received token",tags = ["auth-controller"])
    @GetMapping("/registrationConfirm")
    fun registrationConfirm(@RequestParam("token") token: String): ResponseEntity<String> {
        logger.info("Received token confirmation request for {}", token)
        userServiceExtended.verifyToken(token)
        return ResponseEntity.ok("User account has been confirmed")
    }

    @ApiOperation(value="Get the list of admins",tags = ["auth-controller"])
    @GetMapping("/admins")
    fun getAdmins(): List<UserDetailsDto> {
        logger.info("Searching for the admins")
        return userServiceExtended.getAdmins()
    }

    @ApiOperation(value="Get the list of customers",tags = ["auth-controller","admin"])
    @GetMapping("/customers")
    @OnlyAdmins
    fun getCustomers(): List<UserDetailsDto> {
        logger.info("Searching for customers")
        return userServiceExtended.getCustomers()
    }

    @ApiOperation(value="Update your user's details",tags = ["auth-controller"])
    @PostMapping("/updateUserInfo")
    @OnlyEnabledUsers
    fun updateUserInfo(@RequestParam password: String, @RequestParam name: String, @RequestParam surname: String, @RequestParam deliveryAddr:String): ResponseEntity<*> {
        return ResponseEntity.ok(signInAndUserInfo.updateUserInformation(password, name, surname, deliveryAddr))
    }

    @ApiOperation(value="Get your user's details",tags = ["auth-controller"])
    @GetMapping("/getMyInfo")
    @OnlyEnabledUsers
    fun getMyInformation(): ResponseEntity<UserDetailsDto>{
        return ResponseEntity.ok(signInAndUserInfo.getUserInformation())
    }

}