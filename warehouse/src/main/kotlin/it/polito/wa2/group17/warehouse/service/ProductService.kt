package it.polito.wa2.group17.warehouse.service

import it.polito.wa2.group17.common.dto.NewProductRequest
import it.polito.wa2.group17.common.dto.RatingDto
import it.polito.wa2.group17.common.dto.RatingRequest
import it.polito.wa2.group17.common.exception.EntitiesNotFoundException
import it.polito.wa2.group17.common.exception.EntityNotFoundException
import it.polito.wa2.group17.common.transaction.MultiserviceTransactional
import it.polito.wa2.group17.common.transaction.Rollback
import it.polito.wa2.group17.common.utils.converter.convert
import it.polito.wa2.group17.common.utils.converter.convertTo
import it.polito.wa2.group17.warehouse.dto.*
import it.polito.wa2.group17.warehouse.entity.ProductEntity
import it.polito.wa2.group17.warehouse.entity.RatingEntity
import it.polito.wa2.group17.warehouse.entity.StoredProductEntity
import it.polito.wa2.group17.warehouse.model.Warehouse
import it.polito.wa2.group17.warehouse.repository.ProductRepository
import it.polito.wa2.group17.warehouse.repository.RatingRepository
import it.polito.wa2.group17.warehouse.repository.StoredProductRepository
import it.polito.wa2.group17.warehouse.repository.WarehouseRepository
import org.hibernate.annotations.NotFound
import org.hibernate.sql.Update
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import javax.mail.Store

interface ProductService {
    fun getProductsByCategory(category: String?): List<ProductDto>
    fun getProductById(id: Long): ProductDto
    fun addProduct(product: NewProductRequest): ProductDto
    fun putProductById(productId: Long, productRequest: PutProductRequest): ProductDto
    fun patchProductById(productId: Long, patchProductRequest: PatchProductRequest): ProductDto
    fun deleteProductById(productId: Long): ProductDto
    fun getProductPictureById(productId: Long): PostPicture
    fun addProductPicture(productId: Long, picture: PostPicture): ProductDto
    fun getWarehousesContainingProductById(productId: Long): List<Warehouse>
    fun rateProductById(productId: Long, ratingDto: RatingRequest): UpdateRating?

}

@Service
private open class ProductServiceImpl: ProductService {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(ProductService::class.java)
    }
    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var storedProductRepository: StoredProductRepository

    @Autowired
    private lateinit var ratingRepository: RatingRepository

    @Autowired
    private lateinit var productEntityUpdater: ProductEntityUpdater

    private fun getProductOrThrow(productId: Long) = productRepository.findById(productId).orElseThrow { EntityNotFoundException(productId) }

    override fun getProductsByCategory(category: String?): List<ProductDto> {
        val categ = if(category == "null" ) null else category
        logger.info("Getting products by category ${categ ?: "all"}")
        val result = if(categ != null)
            productRepository.findProductEntitiesByCategory(categ)
        else
            productRepository.findAll().toList()

        if(result.isEmpty()) throw EntitiesNotFoundException(category ?: "all")
        else return result.map{ it.convert() }
    }

    override fun getProductById(productId: Long): ProductDto {
        logger.info("Getting product by Id $productId")
        return getProductOrThrow(productId).convert()
    }

    @MultiserviceTransactional
    override fun addProduct(product: NewProductRequest): ProductDto {
        logger.info("Creating new product")
        val newProduct : ProductEntity = product.convert()
        productRepository.save(newProduct)
        return newProduct.convert()
    }

    @Rollback
    private fun rollbackForAddProduct(product: NewProductRequest,createdProduct: ProductDto){
        logger.warn("Rollback of addProduct")
        productRepository.deleteById(createdProduct.id!!)
    }

    override fun putProductById(
        productId: Long,
        putProductRequest: PutProductRequest
    ): ProductDto {
        logger.info("Putting product by Id $productId")
        return productEntityUpdater.putProduct(productId,putProductRequest).newState!!
    }

    override fun patchProductById(
        productId: Long,
        patchProductRequest: PatchProductRequest
    ): ProductDto {
        logger.info("patching product by Id $productId")
        return productEntityUpdater.patchProduct(productId,patchProductRequest).newState!!
    }

    @MultiserviceTransactional
    override fun deleteProductById(productId: Long): ProductDto {
        logger.info("Deleting product by Id $productId")
        val oldProduct = getProductById(productId)
        productRepository.deleteById(productId)
        return oldProduct.convert()
    }

    @Rollback
    private fun rollbackForDeleteProductById(
        productId: Long,
        oldProduct: ProductDto
    ){
        logger.warn("Rollback of deleteProductById")
        productRepository.save(oldProduct.convert())
    }

    override fun getProductPictureById(
        productId: Long
    ): PostPicture {
        return PostPicture(getProductOrThrow(productId).convert<ProductDto>().pictureURL ?: "")
    }


    override fun addProductPicture(productId: Long, picture: PostPicture): ProductDto {
        var product = getProductOrThrow(productId)
        product.pictureURL = picture.picture
        return productRepository.save(product).convert()
    }

    override fun getWarehousesContainingProductById(productId: Long): List<Warehouse> {
        val product = getProductOrThrow(productId)
        return storedProductRepository
            .findAllByProduct(product)
            .map { it -> it.warehouse.convert() }
    }

    @MultiserviceTransactional
    override fun rateProductById(productId: Long, ratingDto: RatingRequest): UpdateRating? {
        var product = getProductOrThrow(productId)
        val prevRat = product.avgRating
        val newRate = ratingRepository.save(RatingEntity(stars = ratingDto.stars,comment = ratingDto.comment,product = product, title = ratingDto.title, creationDate = Date.from(
            Instant.now())))
        val newAverage = product.ratings.map { it.stars }.toMutableList().let {
            it.add(newRate.stars)
            it.average()
        }
        productRepository.updateRating(productId,newAverage)
        val rateId = newRate.getId()
        return if (rateId == null || prevRat == null) null
        else
            UpdateRating(rateId, prevRat)
    }

    @Rollback
    private fun rollbackForRateProductById(productId: Long, ratingDto: RatingRequest, rating:UpdateRating?) {
        if (rating == null) return
        ratingRepository.deleteById(rating.id_rating)
        productRepository.updateRating(productId, rating.prev_rating)
    }

}

@Service
private open class ProductEntityUpdater {

    data class UpdateTransaction(
        var oldState: ProductDto? = null,
        var newState: ProductDto? = null
    )

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(ProductService::class.java)
    }

    @Autowired
    private lateinit var productRepository: ProductRepository

    @MultiserviceTransactional
    fun putProduct(
        productId: Long,
        putProductRequest: PutProductRequest,
    ): UpdateTransaction {
        logger.info("")
        val update = UpdateTransaction()

        val oldProduct = productRepository.findByIdOrNull(productId)
        update.oldState = oldProduct?.convert()



        val newProduct : ProductEntity = putProductRequest.convert()

        update.newState = newProduct.convert()

        productRepository.save(newProduct)

        return update
    }

    @Rollback
    fun rollbackForPutProduct(
        productId: Long,
        putProductRequest: PutProductRequest,
        update: UpdateTransaction,
    ){
        logger.info("Rollback for putProduct")

        update.oldState?.let {
            productRepository.save(it.convert())
        } ?: productRepository.deleteById(productId)
}

    @MultiserviceTransactional
    fun patchProduct(
        productId: Long,
        productRequest: PatchProductRequest,
    ): UpdateTransaction {
        logger.info("")
        val update = UpdateTransaction()

        val newProduct : ProductEntity = productRequest.convert()

        val oldProduct = productRepository.findById(productId).orElseThrow { EntityNotFoundException(productId) }
        update.oldState = oldProduct.convert()

        newProduct.name?.let { oldProduct.name = it }
        newProduct.description?.let { oldProduct.description = it }
        newProduct.pictureURL?.let { oldProduct.pictureURL = it }
        newProduct.category?.let { oldProduct.category = it }
        newProduct.price?.let { oldProduct.price = it }
        newProduct.avgRating?.let { oldProduct.avgRating = it }
        newProduct.creationDate?.let { oldProduct.creationDate = it }

        update.newState = oldProduct.convert()
        productRepository.save(oldProduct)
        return update
    }

    @Rollback
    fun rollbackForPatchProduct(
        productId: Long,
        productRequest: PatchProductRequest,
        update: UpdateTransaction,
    ){
        logger.info("Rollback for patchProduct")
        productRepository.save(update.oldState!!.convert())
    }


}
