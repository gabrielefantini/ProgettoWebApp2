package it.polito.wa2.group17.warehouse.controller

import it.polito.wa2.group17.warehouse.dto.PatchProductRequest
import it.polito.wa2.group17.warehouse.dto.PutProductRequest
import it.polito.wa2.group17.warehouse.service.ProductService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping(
    value = ["/products"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)

class ProductController {
    @Autowired
    private lateinit var productService: ProductService

    @GetMapping
    fun getProductsByCategory(@RequestParam(name = "category", required = false) category:String) =
        ResponseEntity.ok(productService.getProductsByCategory(category))


    @GetMapping("/{productId}")
    fun getProductById(@PathVariable productId: Long) =
        ResponseEntity.ok(productService.getProductById(productId))

    @PutMapping("/{productId}")
    fun putProductById(
        @PathVariable productId: Long,
        @RequestBody @Valid product: PutProductRequest
    ) = ResponseEntity.ok(
        productService.putProductById(
            productId,
            product,
            productService.getProductById(productId)
        )
    )

    @PatchMapping("/{productId}")
    fun patchProductById(
        @PathVariable productId: Long,
        @RequestBody @Valid product: PatchProductRequest
    ) = ResponseEntity.ok(
        productService.patchProductById(
            productId,
            product,
            productService.getProductById(productId)
        )
    )

    @DeleteMapping("/{productId}")
    fun deleteProductById(
        @PathVariable productId: Long
    ) = ResponseEntity.ok(productService.deleteProductById(productId))


    @GetMapping("/{productID}/picture")
    fun getProductPicture(@PathVariable productId: Long)
    = ResponseEntity.ok(productService.getProductPictureById(productId))

    @PostMapping("/{productID}/picture")
    fun addProductPicture(
        @PathVariable productId: Long,
        @RequestBody @Valid picture: String
    ) = ResponseEntity.ok(productService.addProductPicture(productId, picture))

    @GetMapping("/{productID}/warehouses")
    fun getWarehouseByProductId(
        @PathVariable productId: Long
    ){

    }
}