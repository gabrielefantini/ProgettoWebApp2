package it.polito.wa2.group17.warehouse.controller

import it.polito.wa2.group17.warehouse.dto.*
import it.polito.wa2.group17.warehouse.model.StoredProduct
import it.polito.wa2.group17.warehouse.service.WarehouseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import javax.validation.Valid


@RestController
@RequestMapping(
    value = ["/warehouses"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class WarehouseController {

    @Autowired
    private lateinit var warehouseService: WarehouseService

    @GetMapping
    fun getWarehouses() =
        ResponseEntity.ok(warehouseService.getWarehouses())

    @GetMapping("/{warehouseId}")
    fun getWarehouse(@PathVariable warehouseId: Long) =
        ResponseEntity.ok(warehouseService.getWarehouse(warehouseId))

    @GetMapping("/{warehouseId}/products")
    fun getProductsOfWarehouse(@PathVariable warehouseId: Long) =
        ResponseEntity.ok(warehouseService.getWarehouse(warehouseId).products)

    @GetMapping("/{warehouseId}/products/{productId}")
    fun getProductOfWarehouse(
        @PathVariable warehouseId: Long,
        @PathVariable productId: Long
    ): ResponseEntity<StoredProduct> {
        val product = warehouseService.getWarehouse(warehouseId).products.find { it.productId == productId }
        return if (product == null)
            ResponseEntity.notFound().build()
        else ResponseEntity.ok(product)
    }

    /////////////////////////////////////////////////////////////////////////////////////

    @PostMapping
    fun saveWarehouse(
        @RequestBody @Valid warehouseRequest: WarehouseRequest,
    ): ResponseEntity<*> {
        val products = warehouseRequest.products.map { StoredProduct(it.productId, it.quantity, it.minimumQuantity) }
        val warehouse = warehouseService.createWarehouse(products)
        /*return ResponseEntity
            .created(URI.create("/warehouses/${warehouse.id}"))
            .body(warehouse)*/
        return ResponseEntity.ok(warehouse.id)
    }

    @DeleteMapping("/{warehouseId}")
    fun deleteWarehouse(@PathVariable warehouseId: Long) =
        ResponseEntity.ok(warehouseService.deleteWarehouse(warehouseId))


    @PostMapping("/sell")
    fun sellProductFromAnyWarehouse(@RequestBody @Valid sellRequest: SellRequest) =
        ResponseEntity.ok(warehouseService.sellProductFromAnywhere(sellRequest))

    @PostMapping("/{warehouseId}/sell")
    fun sellProductFromWarehouse(
        @PathVariable warehouseId: Long,
        @RequestBody @Valid sellRequest: SellRequest
    ) =
        ResponseEntity.ok(warehouseService.sellProduct(warehouseId, sellRequest))

    @PostMapping("/{warehouseId}/fulfill")
    fun fulfillWarehouse(@PathVariable warehouseId: Long, @RequestBody @Valid fulfillRequest: FulfillRequest) =
        ResponseEntity.ok(warehouseService.fulfillProduct(warehouseId, fulfillRequest))


    @PostMapping("/{warehouseId}/products")
    fun addProductToWarehouse(
        @PathVariable warehouseId: Long,
        @RequestBody @Valid addProductRequest: AddProductRequest
    ): ResponseEntity<StoredProduct> {
        val storedProduct = warehouseService.addProductToWarehouse(warehouseId, addProductRequest)
        return ResponseEntity.created(URI.create("/warehouse/${warehouseId}/${storedProduct.productId}"))
            .body(storedProduct)
    }

    @DeleteMapping("/{warehouseId}/products/{productId}")
    fun removeProductFromWarehouse(
        @PathVariable warehouseId: Long,
        @PathVariable productId: Long,
    ) = ResponseEntity.ok(warehouseService.removeProductFromWarehouse(warehouseId, productId))

    @DeleteMapping("/{warehouseId}/products")
    fun removeProductsFromWarehouse(
        @PathVariable warehouseId: Long
    ) = ResponseEntity.ok(warehouseService.removeAllProductsFromWarehouse(warehouseId))

    @PatchMapping("/{warehouseId}/products/{productId}")
    fun updateProductLimit(
        @PathVariable warehouseId: Long,
        @PathVariable productId: Long,
        @RequestBody @Valid updateProductRequest: UpdateProductRequest
    ) = ResponseEntity.ok(warehouseService.updateProductLimit(warehouseId, productId, updateProductRequest))
}
