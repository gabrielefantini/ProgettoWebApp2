package it.polito.wa2.group17.catalog.controller

import it.polito.wa2.group17.catalog.dto.UserDetailsDto
import it.polito.wa2.group17.catalog.security.OnlyEnabledUsers
import it.polito.wa2.group17.catalog.service.CatalogService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
    fun getOrdersByUsername(): ResponseEntity<Unit>/*OrderDto*/ {
        return ResponseEntity.ok(catalogService.getOrders())
    }

    @GetMapping ("/order/{orderId}")
    @OnlyEnabledUsers
    fun getOrderById(@PathVariable orderId: Long): ResponseEntity<Unit>/*OrderDto*/ {
        return ResponseEntity.ok(catalogService.getOrderById(orderId))
    }

    @PostMapping ("/order")
    @OnlyEnabledUsers
    fun addOrder(@RequestBody order: String/*OrderDto*/): Long {
        val orderId = catalogService.addNewOrder(order)
        return orderId
    }

    // tutti possono elencare i prodotti
    @GetMapping("/products")
    fun getProducts(): ResponseEntity<Unit> {
        return ResponseEntity.ok(catalogService.listProducts())
    }

    @GetMapping("/product/{productId}")
    fun getProductById(@PathVariable productId: Long): ResponseEntity<Unit> {
        return ResponseEntity.ok(catalogService.getProduct(productId))
    }

    @GetMapping("/mywallets")
    @OnlyEnabledUsers
    fun getMyWallets(): ResponseEntity<Unit> {
        return ResponseEntity.ok(catalogService.getWallets())
    }

    @GetMapping("/getUserInfo")
    @OnlyEnabledUsers
    fun getMyInformation(): ResponseEntity<UserDetailsDto>{
        return ResponseEntity.ok(catalogService.getUserInformation())
    }

    @PostMapping("/updateUserInfo")
    @OnlyEnabledUsers
    fun updateUserInfo(@RequestBody username: String, @RequestBody email: String, @RequestBody name: String, @RequestBody surname: String, @RequestBody deliveryAddr:String): ResponseEntity<Long> {
        return ResponseEntity.ok(catalogService.updateUserInformation(username, email, name, surname, deliveryAddr))
    }
}
