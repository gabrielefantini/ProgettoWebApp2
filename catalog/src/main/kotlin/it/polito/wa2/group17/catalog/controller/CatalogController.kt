package it.polito.wa2.group17.catalog.controller

import io.swagger.annotations.ApiOperation
import it.polito.wa2.group17.catalog.security.OnlyAdmins
import it.polito.wa2.group17.catalog.security.OnlyEnabledUsers
import it.polito.wa2.group17.catalog.service.CatalogService
import it.polito.wa2.group17.common.dto.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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

    // order-service ***

    @ApiOperation(value="Get the list of your orders",tags = ["order","customer","catalog-controller"])
    @GetMapping ("/myOrders")
    @OnlyEnabledUsers
    fun getMyOrders(): ResponseEntity<List<OrderDto>> {
        return ResponseEntity.ok(catalogService.getMyOrders())
    }

    @ApiOperation(value="Get the list of orders",tags = ["order","admin","catalog-controller"])
    @GetMapping ("/orders")
    @OnlyAdmins
    fun getOrders(): ResponseEntity<List<OrderDto>> {
        return ResponseEntity.ok(catalogService.getOrders())
    }

    @ApiOperation(value="Get a given order details",tags = ["order","customer","admin","catalog-controller"])
    @GetMapping ("/orders/{orderId}")
    @OnlyEnabledUsers
    fun getOrderById(@PathVariable orderId: Long): ResponseEntity<OrderDto> {
        return ResponseEntity.ok(catalogService.getOrderById(orderId))
    }

    @ApiOperation(value="Get the status of a given order",tags = ["order","customer","admin","catalog-controller"])
    @GetMapping("/orders/{orderId}/status")
    @OnlyEnabledUsers
    fun getOrderStatus(@PathVariable orderId: Long): ResponseEntity<OrderStatus?>{
        return ResponseEntity.ok(catalogService.getOrderStatus(orderId))
    }

    @ApiOperation(value="Add a new order request",tags = ["order","customer","catalog-controller"])
    @PostMapping("/orders")
    @OnlyEnabledUsers
    fun addOrder(@RequestBody @Valid order: NewOrderRequest): Long {
        return catalogService.addNewOrder(order)
    }

    @ApiOperation(value="Change the order status, making it progress",tags = ["order","admin","catalog-controller"])
    @PatchMapping("/orders/{orderId}/status")
    @OnlyAdmins
    fun changeOrderStatus(@PathVariable orderId: Long, @RequestBody @Valid status: OrderPatchRequest): ResponseEntity<Long> {
        return ResponseEntity.ok(catalogService.changeOrderStatus(orderId, status))
    }

    @ApiOperation(value="Request the cancellation of one of your orders",tags = ["order","customer","catalog-controller"])
    @DeleteMapping("/orders/{orderId}")
    @OnlyEnabledUsers
    fun cancelOrder(@PathVariable orderId: Long): ResponseEntity<Unit> {
        return ResponseEntity.ok(catalogService.cancelUserOrder(orderId))
    }

    // ******

    // wallet-service ***

    @ApiOperation(value="Get your wallet details",tags = ["wallet","customer","catalog-controller"])
    @GetMapping("/wallets")
    @OnlyEnabledUsers
    fun getMyWallet(): ResponseEntity<Wallet> {
        return ResponseEntity.ok(catalogService.getWallets())
    }

    @ApiOperation(value="Add a new wallet for a given user",tags = ["wallet","admin","catalog-controller"])
    @PostMapping("/wallets")
    @OnlyAdmins
    fun addWalletToUser(@RequestParam userId: Long): ResponseEntity<Wallet>{
        return ResponseEntity.ok(catalogService.addWalletToUser(userId))
    }

    @ApiOperation(value="Perform a transaction for a given wallet",tags = ["wallet","admin","catalog-controller"])
    @PostMapping("/wallets/{walletId}/transactions")
    @OnlyAdmins
    fun performTransaction(@PathVariable walletId: Long,@RequestParam amount: Long): ResponseEntity<*>{
        return ResponseEntity.ok(catalogService.performTransaction(walletId,amount))
    }

    // ******

    // warehouse-service / products ***

    @ApiOperation(value="Get the list of products (optionally by category)",tags = ["product","customer","admin","catalog-controller"])
    @GetMapping("/products")
    fun getProductsByCategory(@RequestParam(name = "category", required = false) category:String?) = ResponseEntity.ok(catalogService.getProductsByCategory(category))

    @ApiOperation(value="Get a given product's details",tags = ["product","customer","admin","catalog-controller"])
    @GetMapping("/products/{productId}")
    fun getProductById(@PathVariable productId: Long) =
        ResponseEntity.ok(catalogService.getProduct(productId))

    @ApiOperation(value="Get a given product's picture",tags = ["product","customer","admin","catalog-controller"])
    @GetMapping("/products/{productId}/picture")
    fun getPicture(@PathVariable productId: Long) =
        ResponseEntity.ok(catalogService.getPicture(productId))

    @ApiOperation(value="Add a new product",tags = ["product","admin","catalog-controller"])
    @PostMapping("/products")
    @OnlyAdmins
    fun addProduct(@RequestBody @Valid newProductRequest: NewProductRequest) = catalogService.addProduct(newProductRequest)

    @ApiOperation(value="Add your rating to a given product",tags = ["product","customer","catalog-controller"])
    @PostMapping("/products/{productId}/rating")
    @OnlyEnabledUsers
    fun rateProduct(@PathVariable productId: Long, @RequestBody @Valid ratingRequest: RatingRequest): ResponseEntity<Long?> {
        //val rating = RatingDto(null, ratingRequest.stars, ratingRequest.comment)
        return ResponseEntity.ok(catalogService.rateProduct(productId, ratingRequest))
    }

    @ApiOperation(value="Add a picture URL to a given product",tags = ["product","admin","catalog-controller"])
    @PutMapping("/products/{productId}/picture")
    @OnlyAdmins
    fun addPicture(@PathVariable productId: Long, @RequestBody @Valid picture: PostPicture) =
        ResponseEntity.ok(catalogService.addProductPicture(productId, picture))

    @ApiOperation(value="Modify a given product's details",tags = ["product","admin","catalog-controller"])
    @PatchMapping("/products/{productId}")
    @OnlyAdmins
    fun patchProductById(@PathVariable productId: Long, @RequestBody @Valid product: PatchProductRequest) =
        ResponseEntity.ok(catalogService.patchProductById(productId,product))

    @ApiOperation(value="Delete a given product",tags = ["product","admin","catalog-controller"])
    @DeleteMapping("/products/{productId}")
    @OnlyAdmins
    fun deleteProduct(@PathVariable productId: Long) = catalogService.deleteProduct(productId)

    // ******

    // warehouse-service / warehouses ***

    @ApiOperation(value="Get the list of warehouses",tags = ["warehouse","admin","catalog-controller"])
    @GetMapping("/warehouses")
    @OnlyAdmins
    fun getWarehouses() = ResponseEntity.ok(catalogService.getWarehouses())

    @ApiOperation(value="Add a new warehouse",tags = ["warehouse","admin","catalog-controller"])
    @PostMapping("/warehouses")
    @OnlyAdmins
    fun addWarehouse(@RequestBody @Valid warehouseRequest: WarehouseRequest) =
        ResponseEntity.ok(catalogService.addWarehouse(warehouseRequest))

    @ApiOperation(value="Add a product to a given warehouse",tags = ["warehouse","admin","catalog-controller"])
    @PostMapping("/warehouses/{warehouseId}/products")
    @OnlyAdmins
    fun addProductToWarehouse(@PathVariable warehouseId: Long, @RequestBody @Valid addProductRequest: AddProductRequest
    ): ResponseEntity<StoredProductDto?> {
        return ResponseEntity.ok(catalogService.addProductToWarehouse(warehouseId, addProductRequest))
    }

    @ApiOperation(value="Delete a given warehouse",tags = ["warehouse","admin","catalog-controller"])
    @DeleteMapping("/warehouses/{warehouseId}")
    @OnlyAdmins
    fun deleteWarehouse(@PathVariable warehouseId: Long) =
        catalogService.deleteWarehouse(warehouseId)

    // ******

}
