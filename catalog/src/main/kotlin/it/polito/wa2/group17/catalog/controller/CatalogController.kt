package it.polito.wa2.group17.catalog.controller

import it.polito.wa2.group17.catalog.dto.UserDetailsDto
import it.polito.wa2.group17.catalog.security.OnlyEnabledUsers
import it.polito.wa2.group17.catalog.service.CatalogService
import it.polito.wa2.group17.common.dto.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.net.URI
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

}
