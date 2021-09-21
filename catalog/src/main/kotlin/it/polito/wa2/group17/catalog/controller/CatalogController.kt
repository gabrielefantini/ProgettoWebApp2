package it.polito.wa2.group17.catalog.controller

import it.polito.wa2.group17.common.dto.StoredProductDto
import it.polito.wa2.group17.catalog.dto.UserDetailsDto
import it.polito.wa2.group17.catalog.security.OnlyEnabledUsers
import it.polito.wa2.group17.catalog.service.CatalogService
import it.polito.wa2.group17.common.dto.OrderDto
import it.polito.wa2.group17.common.dto.PostPicture
import it.polito.wa2.group17.common.dto.Wallet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
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

    @GetMapping("/getUserInfo")
    @OnlyEnabledUsers
    fun getMyInformation(): ResponseEntity<UserDetailsDto>{
        return ResponseEntity.ok(catalogService.getUserInformation())
    }

    @PutMapping("/cancelOrder/{orderId}")
    @OnlyEnabledUsers
    fun cancelOrder(@PathVariable orderId: Long): ResponseEntity<Unit> {
        return ResponseEntity.ok(catalogService.cancelUserOrder(orderId))
    }
}
