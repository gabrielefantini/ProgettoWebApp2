package it.polito.wa2.group17.catalog.controller

import it.polito.wa2.group17.catalog.dto.UserDetailsDto
import it.polito.wa2.group17.catalog.model.LoginRequest
import it.polito.wa2.group17.catalog.model.UserRegistration
import it.polito.wa2.group17.catalog.security.OnlyAdmins
import it.polito.wa2.group17.catalog.security.OnlyEnabledUsers
import it.polito.wa2.group17.catalog.security.jwt.JwtUtils
import it.polito.wa2.group17.catalog.service.CatalogService
import it.polito.wa2.group17.catalog.service.SignInAndUserInfo
import it.polito.wa2.group17.catalog.service.UserDetailsServiceExtended
import it.polito.wa2.group17.common.dto.*
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
    value = ["/catalog"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class CatalogController {
    @Autowired
    private lateinit var catalogService: CatalogService


    @GetMapping ("/myorders")
    @OnlyEnabledUsers
    fun getOrdersByUsername(): ResponseEntity<List<OrderDto>> {
        return ResponseEntity.ok(catalogService.getOrders())
    }

    @GetMapping ("/order/{orderId}")
    @OnlyEnabledUsers
    fun getOrderById(@PathVariable orderId: Long): ResponseEntity<OrderDto> {
        return ResponseEntity.ok(catalogService.getOrderById(orderId))
    }

    @PostMapping("/order")
    @OnlyEnabledUsers
    fun addOrder(@RequestBody order: OrderDto): Long {
        return catalogService.addNewOrder(order)
    }

    // tutti possono elencare i prodotti
    @GetMapping("/products")
    fun getProducts(): ResponseEntity<List<StoredProductDto>> {
        return ResponseEntity.ok(catalogService.listProducts())
    }

    @GetMapping("/product/{productId}")
    fun getProductById(@PathVariable productId: Long): ResponseEntity<StoredProductDto> {
        return ResponseEntity.ok(catalogService.getProduct(productId))
    }

    @GetMapping("/product/{productId}/picture")
    fun getPicture(@PathVariable productId: Long): ResponseEntity<PostPicture> {
        return ResponseEntity.ok(catalogService.getPicture(productId))
    }

    @GetMapping("/mywallets")
    @OnlyEnabledUsers
    fun getMyWallets(): ResponseEntity<Wallet> {
        return ResponseEntity.ok(catalogService.getWallets())
    }

    @PutMapping("/cancelOrder/{orderId}")
    @OnlyEnabledUsers
    fun cancelOrder(@PathVariable orderId: Long): ResponseEntity<Unit> {
        return ResponseEntity.ok(catalogService.cancelUserOrder(orderId))
    }

    @PutMapping("/{productId}/picture")
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    fun addPicture(@PathVariable productId: Long, @RequestBody @Valid picture: PostPicture): ResponseEntity<ProductDto?> {
        return ResponseEntity.ok(catalogService.addProductPicture(productId, picture))
    }

    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/patchproduct/{productId}")
    fun patchProductById(@PathVariable productId: Long, @RequestBody @Valid product: PatchProductRequest) =
        ResponseEntity.ok(catalogService.patchProductById(productId,product)
    )

    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/{warehouseId}/add/product")
    fun addProductToWarehouse(@PathVariable warehouseId: Long, @RequestBody @Valid addProductRequest: AddProductRequest
    ): ResponseEntity<StoredProductDto?> {
        return ResponseEntity.ok(catalogService.addProductToWarehouse(warehouseId, addProductRequest))
    }

    @GetMapping("/status/{orderId}")
    @OnlyEnabledUsers
    fun getOrderStatus(@PathVariable orderId: Long): ResponseEntity<OrderStatus?>{
        return ResponseEntity.ok(catalogService.getOrderStatus(orderId))
    }

    @PostMapping("/{productId}/rating")
    @OnlyEnabledUsers
    fun rateProduct(@PathVariable productId: Long, @RequestBody @Valid ratingRequest: RatingRequest): ResponseEntity<Long?> {
        //val rating = RatingDto(null, ratingRequest.stars, ratingRequest.comment)
        return ResponseEntity.ok(catalogService.rateProduct(productId, ratingRequest))
    }

    @PatchMapping("/{productId}/changeStatus")
    @OnlyAdmins
    fun changeStatus(@PathVariable productId: Long, @RequestBody @Valid status: OrderPatchRequest): ResponseEntity<Long> {
        return ResponseEntity.ok(catalogService.changeProductStatus(productId, status))
    }

    @PutMapping("/addWarehouse")
    @OnlyAdmins
    fun addWarehouse(@RequestBody @Valid warehouseRequest: WarehouseRequest): ResponseEntity<Long?> {
        return ResponseEntity.ok(catalogService.addWarehouse(warehouseRequest))
    }

    @PutMapping("/addProductToWarehouse")
    @OnlyAdmins
    fun addProductToWarehouseFun(@RequestParam warehouseId: Long, @RequestBody @Valid putProductRequest: AddProductRequest): ResponseEntity<Long?> {
        return ResponseEntity.ok(catalogService.addProductToWarehouse(warehouseId, putProductRequest)?.productId)
    }

    @DeleteMapping("/deleteWarehouse/warehouseId")
    @OnlyAdmins
    fun deleteWarehouse(@RequestParam warehouseId: Long) = catalogService.deleteWarehouse(warehouseId)


    @DeleteMapping("/deleteProduct/productId")
    @OnlyAdmins
    fun deleteProduct(@RequestParam productId: Long) = catalogService.deleteProduct(productId)

    @PutMapping("/addProduct")
    @OnlyAdmins
    fun addProduct(@RequestParam productId: Long, @RequestBody @Valid putProductRequest: PutProductRequest) = catalogService.addProduct(productId, putProductRequest)

    //////////////////////////////////////////////////////////////////////////////////////////////////////// LOGIN

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

    @PostMapping("/login/register")
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

    @GetMapping("/login/register/renewToken")
    fun renewToken(
        @RequestParam username: String
    ): ResponseEntity<*> {
        logger.info("Received token renew request from {}", username)
        userServiceExtended.createTokenForUser(username)
        return ResponseEntity.ok().build<Any>()
    }

    @PostMapping("/login/signin")
    fun signIn(
        @Valid @RequestBody req: LoginRequest
    ): ResponseEntity<*> {
        logger.info("User {} is trying to log in", req.username)
        return signInAndUserInfo.signInUser(req)
    }

    @PutMapping("/login/setAdmin")
    fun setAdmin(@RequestParam username: String, @RequestParam value: Boolean): ResponseEntity<Long>{
        print("Inside setAdmin")
        return ResponseEntity.ok(userDetails.setUserAsAdmin(username, value)?.id)
    }

    @GetMapping("/login/registrationConfirm")
    fun registrationConfirm(@RequestParam("token") token: String): ResponseEntity<String> {
        logger.info("Received token confirmation request for {}", token)
        userServiceExtended.verifyToken(token)
        return ResponseEntity.ok("User account has been confirmed")
    }

    @GetMapping("/login/admins")
    fun getAdmins(): List<UserDetailsDto> {
        logger.info("Searching for the admins")
        return userServiceExtended.getAdmins()
    }

    @PostMapping("/login/updateUserInfo")
    @OnlyEnabledUsers
    fun updateUserInfo(@RequestParam password: String, @RequestParam name: String, @RequestParam surname: String, @RequestParam deliveryAddr:String): ResponseEntity<*> {
        return ResponseEntity.ok(signInAndUserInfo.updateUserInformation(password, name, surname, deliveryAddr))
    }

    @GetMapping("/login/getUserInfo")
    @OnlyEnabledUsers
    fun getMyInformation(): ResponseEntity<UserDetailsDto>{
        return ResponseEntity.ok(signInAndUserInfo.getUserInformation())
    }

    @GetMapping("/login/findByUsername/{username}")
    fun findUserByUsername(@PathVariable username:String):ResponseEntity<UserDetailsDto>? {
        val user = signInAndUserInfo.findUser(username)
        return if (user != null) ResponseEntity.ok(UserDetailsDto(user.id, user.username, "Password not available!", user.email, user.isEnabled, user.roles, user.name, user.surname, user.deliveryAddr))
        else return null
    }

}
