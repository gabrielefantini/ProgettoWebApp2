package it.polito.wa2.group17.warehouse.service

import it.polito.wa2.group17.common.exception.EntitiesNotFoundException
import it.polito.wa2.group17.common.exception.EntityNotFoundException
import it.polito.wa2.group17.common.transaction.MultiserviceTransactional
import it.polito.wa2.group17.common.transaction.Rollback
import it.polito.wa2.group17.common.utils.converter.convert
import it.polito.wa2.group17.common.utils.converter.convertTo
import it.polito.wa2.group17.warehouse.dto.PatchProductRequest
import it.polito.wa2.group17.warehouse.dto.ProductDto
import it.polito.wa2.group17.warehouse.dto.PutProductRequest
import it.polito.wa2.group17.warehouse.entity.ProductEntity
import it.polito.wa2.group17.warehouse.entity.StoredProductEntity
import it.polito.wa2.group17.warehouse.repository.ProductRepository
import org.hibernate.annotations.NotFound
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

interface ProductService {
    fun getProductsByCategory(category: String): List<ProductDto>
    fun getProductById(id: Long): ProductDto
    fun putProductById(productId: Long, productRequest: PutProductRequest): ProductDto
    fun patchProductById(productId: Long, patchProductRequest: PatchProductRequest): ProductDto
    fun deleteProductById(productId: Long): ProductDto
    fun getProductPictureById(productId: Long): String
    fun addProductPicture(productId: Long, picture: String): ProductDto

}

@Service
private open class ProductServiceImpl: ProductService {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(ProductService::class.java)
    }
    @Autowired
    private lateinit var productRepository: ProductRepository

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
    ): String {
        return getProductOrThrow(productId).convert<ProductDto>().pictureURL ?: ""
    }

    //TODO vedere se fare rollback o no
    override fun addProductPicture(productId: Long, picture: String): ProductDto {
        var product = getProductOrThrow(productId)
        product.pictureURL = picture
        return productRepository.save(product).convert()
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

        val oldProduct = productRepository.findById(productId).get()
        update.oldState = oldProduct

        val newProduct : ProductEntity = putProductRequest.convert()
        update.newState = newProduct

        productRepository.save(newProduct)
        return update
    }

    @Rollback
    fun rollBackForPutProduct(
        productId: Long,
        putProductRequest: PutProductRequest,
        update: UpdateTransaction,
    ){
        logger.info("")
        update.oldState?.let {
            //entity modified -> save older status
            productRepository.save(it)
        }?: run{
            //entity created -> remove new status
            productRepository.delete(update.newState!!)
        }
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
        update.oldState = oldProduct

        newProduct.name?.let { oldProduct.name = it }
        newProduct.description?.let { oldProduct.description = it }
        newProduct.pictureURL?.let { oldProduct.pictureURL = it }
        newProduct.category?.let { oldProduct.category = it }
        newProduct.price?.let { oldProduct.price = it }
        newProduct.avgRating?.let { oldProduct.avgRating = it }
        newProduct.creationDate?.let { oldProduct.creationDate = it }

        productRepository.save(oldProduct)
        return update
    }

    @Rollback
    fun rollBackForPatchProduct(
        productId: Long,
        productRequest: PatchProductRequest,
        update: UpdateTransaction,
    ){
        logger.info("")
        productRepository.save(update.oldState!!)
    }


}