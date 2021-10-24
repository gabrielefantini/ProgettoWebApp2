package it.polito.wa2.group17.warehouse.controller

import it.polito.wa2.group17.common.dto.NewProductRequest
import it.polito.wa2.group17.common.dto.RatingDto
import it.polito.wa2.group17.common.dto.RatingRequest
import it.polito.wa2.group17.warehouse.dto.PatchProductRequest
import it.polito.wa2.group17.warehouse.dto.PostPicture
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
    fun getProductsByCategory(@RequestParam(name = "category", required = false) category:String?) =
        ResponseEntity.ok(productService.getProductsByCategory(category))


    @GetMapping("/{productId}")
    fun getProductById(@PathVariable productId: Long) =
        ResponseEntity.ok(productService.getProductById(productId))

    @PostMapping
    fun addProduct(@RequestBody @Valid product: NewProductRequest) =
        ResponseEntity.ok(productService.addProduct(product))

    @PutMapping("/{productId}")
    fun putProductById(
        @PathVariable productId: Long,
        @RequestBody @Valid product: PutProductRequest
    ) = ResponseEntity.ok(
        productService.putProductById(
            productId,
            product
        )
    )

    @PatchMapping("/{productId}")
    fun patchProductById(
        @PathVariable productId: Long,
        @RequestBody @Valid product: PatchProductRequest
    ) = ResponseEntity.ok(
        productService.patchProductById(
            productId,
            product
        )
    )

    @DeleteMapping("/{productId}")
    fun deleteProductById(
        @PathVariable productId: Long
    ) = ResponseEntity.ok(productService.deleteProductById(productId))


    @GetMapping("/{productId}/picture")
    fun getProductPicture(@PathVariable productId: Long)
    = ResponseEntity.ok(productService.getProductPictureById(productId))

    @PostMapping("/{productId}/picture")
    fun addProductPicture(
        @PathVariable productId: Long,
        @RequestBody @Valid picture: PostPicture
    ) = ResponseEntity.ok(productService.addProductPicture(productId, picture))

    @GetMapping("/{productId}/warehouses")
    fun getWarehouseByProductId(
        @PathVariable productId: Long
    ) = ResponseEntity.ok(productService.getWarehousesContainingProductById(productId))

    @PostMapping("/{productId}/rating")
    fun rateProduct(@PathVariable productId: Long,
                    @RequestBody @Valid ratingDto: RatingRequest) = ResponseEntity.ok(productService.rateProductById(productId, ratingDto)?.id_rating)
}
