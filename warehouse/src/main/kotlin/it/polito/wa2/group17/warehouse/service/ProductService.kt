package it.polito.wa2.group17.warehouse.service

import it.polito.wa2.group17.common.dto.RatingDto
import it.polito.wa2.group17.common.dto.RatingRequest
import it.polito.wa2.group17.common.exception.EntitiesNotFoundException
import it.polito.wa2.group17.common.exception.EntityNotFoundException
import it.polito.wa2.group17.common.transaction.MultiserviceTransactional
import it.polito.wa2.group17.common.transaction.Rollback
import it.polito.wa2.group17.common.utils.converter.convert
import it.polito.wa2.group17.common.utils.converter.convertTo
import it.polito.wa2.group17.warehouse.dto.PatchProductRequest
import it.polito.wa2.group17.warehouse.dto.PostPicture
import it.polito.wa2.group17.warehouse.dto.ProductDto
import it.polito.wa2.group17.warehouse.dto.PutProductRequest
import it.polito.wa2.group17.warehouse.entity.ProductEntity
import it.polito.wa2.group17.warehouse.entity.RatingEntity
import it.polito.wa2.group17.warehouse.entity.StoredProductEntity
import it.polito.wa2.group17.warehouse.model.Warehouse
import it.polito.wa2.group17.warehouse.repository.ProductRepository
import it.polito.wa2.group17.warehouse.repository.RatingRepository
import it.polito.wa2.group17.warehouse.repository.StoredProductRepository
import it.polito.wa2.group17.warehouse.repository.WarehouseRepository
import org.hibernate.annotations.NotFound
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import javax.mail.Store

interface ProductService {
    fun getProductsByCategory(category: String): List<ProductDto>
    fun getProductById(id: Long): ProductDto
    fun putProductById(productId: Long, productRequest: PutProductRequest): ProductDto
    fun patchProductById(productId: Long, patchProductRequest: PatchProductRequest): ProductDto
    fun deleteProductById(productId: Long): ProductDto
    fun getProductPictureById(productId: Long): PostPicture
    fun addProductPicture(productId: Long, picture: PostPicture): ProductDto
    fun getWarehousesContainingProductById(productId: Long): List<Warehouse>
    fun rateProductById(productId: Long, ratingDto: RatingRequest): Long?

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

    override fun getProductsByCategory(category: String): List<ProductDto> {
        logger.info("Getting products by category")
        val result = productRepository.findProductEntitiesByCategory(category)
        if(result.isEmpty()) throw EntitiesNotFoundException(category)
        else return result.map{ it.convert() }
    }

    override fun getProductById(productId: Long): ProductDto {
        logger.info("Getting product by Id")
        return getProductOrThrow(productId).convert()
    }

    override fun putProductById(
        productId: Long,
        putProductRequest: PutProductRequest
    ): ProductDto {
        logger.info("Putting product by Id")
        return productEntityUpdater.putProduct(productId,putProductRequest).newState!!.convert()
    }

    override fun patchProductById(
        productId: Long,
        patchProductRequest: PatchProductRequest
    ): ProductDto {
        logger.info("patching product by Id")
        return productEntityUpdater.patchProduct(productId,patchProductRequest).newState!!.convert()
    }

    @MultiserviceTransactional
    override fun deleteProductById(productId: Long): ProductDto {
        logger.info("Deleting product by Id")
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
    override fun rateProductById(productId: Long, ratingDto: RatingRequest): Long? {
        val product = productRepository.findById(productId)
        if (product.isEmpty) return null
        else {
            val id = ratingRepository.count()
            val entity = ratingRepository.save(RatingEntity(id, ratingDto.stars, ratingDto.comment, product.get()))
            return entity.getId()
        }
    }

    @Rollback
    private fun rollbackForRateProductById(productId: Long, ratingDto: RatingRequest, id:Long?) {
        if (id == null) return
        ratingRepository.deleteById(id)
    }

}

@Service
private open class ProductEntityUpdater {

    data class UpdateTransaction(
        var oldState: ProductEntity? = null,
        var newState: ProductEntity? = null
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
        update.oldState = oldProduct?.convert<ProductDto>()?.convert()



        val newProduct : ProductEntity = putProductRequest.convert()

        update.newState = newProduct.convert<ProductDto>().convert()

        productRepository.save(newProduct)

        return update
    }

    @Rollback
    fun rollbackForPutProduct(
        productId: Long,
        putProductRequest: PutProductRequest,
        update: UpdateTransaction,
    ){
        logger.info("Rollback Of PutProduct")

        update.oldState?.let {
            productRepository.save(it)
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
        update.oldState = oldProduct.convert<ProductDto>().convert()

        newProduct.name?.let { oldProduct.name = it }
        newProduct.description?.let { oldProduct.description = it }
        newProduct.pictureURL?.let { oldProduct.pictureURL = it }
        newProduct.category?.let { oldProduct.category = it }
        newProduct.price?.let { oldProduct.price = it }
        newProduct.avgRating?.let { oldProduct.avgRating = it }
        newProduct.creationDate?.let { oldProduct.creationDate = it }

        update.newState = oldProduct
        productRepository.save(oldProduct)
        return update
    }

    @Rollback
    fun rollbackForPatchProduct(
        productId: Long,
        productRequest: PatchProductRequest,
        update: UpdateTransaction,
    ){
        logger.info("")
        productRepository.save(update.oldState!!)
    }


}
