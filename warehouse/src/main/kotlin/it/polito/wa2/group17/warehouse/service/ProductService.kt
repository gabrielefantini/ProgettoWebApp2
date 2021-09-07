package it.polito.wa2.group17.warehouse.service

import it.polito.wa2.group17.common.transaction.MultiserviceTransactional
import it.polito.wa2.group17.common.transaction.Rollback
import it.polito.wa2.group17.common.utils.converter.convert
import it.polito.wa2.group17.common.utils.converter.convertTo
import it.polito.wa2.group17.warehouse.dto.PatchProductRequest
import it.polito.wa2.group17.warehouse.dto.ProductDto
import it.polito.wa2.group17.warehouse.dto.PutProductRequest
import it.polito.wa2.group17.warehouse.entity.ProductEntity
import it.polito.wa2.group17.warehouse.repository.ProductRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

interface ProductService {
    fun getProductsByCategory(category: String?): List<ProductDto>
    fun getProductById(id: Long): ProductDto
    fun putProductById(productId: Long, productRequest: PutProductRequest, oldProduct: ProductDto): ProductDto
    fun patchProductById(productId: Long, productRequest: PatchProductRequest, oldProduct: ProductDto): ProductDto
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

    override fun getProductsByCategory(category: String?): List<ProductDto> {
        logger.info("Getting products by category")
        return productRepository.findProductEntitiesByCategory(category)
            .map{ it -> it.convert() }
    }

    override fun getProductById(productId: Long): ProductDto {
        logger.info("Getting product by Id")
        return productRepository
            .findById(productId)
            .convert()
    }

    //TODO riprendere da qua domani
    @MultiserviceTransactional
    override fun putProductById(
        productId: Long,
        putProductRequest: PutProductRequest,
        oldProduct: ProductDto
    ): ProductDto {
        logger.info("Putting product by Id")
        putProductRequest.id = productId
        //TODO vedere che capita
        return productRepository
            .save(putProductRequest.convert())
            .convert()
    }

    @Rollback
    private fun rollbackForPutProductById(
        productId: Long,
        putProductRequest: PutProductRequest,
        oldProduct: ProductDto,
        newProduct: ProductDto
    ){
        logger.warn("Rollback for PutProductById")
        productRepository.save(oldProduct.convert())
    }

    @MultiserviceTransactional
    override fun patchProductById(
        productId: Long,
        productRequest: PatchProductRequest,
        oldProduct: ProductDto): ProductDto {
        logger.info("patching product by Id")
        var newProduct = oldProduct

        productRequest.name?.let { newProduct.name = it }
        productRequest.description?.let { newProduct.description = it }
        productRequest.pictureURL?.let { newProduct.pictureURL = it }
        productRequest.category?.let { newProduct.category = it }
        productRequest.price?.let { newProduct.price = it }
        productRequest.avgRating?.let { newProduct.avgRating = it }
        productRequest.creationDate?.let { newProduct.creationDate = it }

        return productRepository
            .save(newProduct.convert())
            .convert()
    }

    @Rollback
    private fun rollbackForPatchProductById(
        productId: Long,
        productRequest: PatchProductRequest,
        oldProduct: ProductDto
    ){
        logger.warn("Rollback of PatchProductById")
        productRepository.save(oldProduct.convert())
    }

    @MultiserviceTransactional
    override fun deleteProductById(productId: Long): ProductDto {
        logger.info("Deleting product by Id")
        val oldProduct = productRepository.findById(productId)
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
        return productRepository.findById(productId)?.convert<ProductDto>().pictureURL
    }

    //TODO vedere se fare rollback o no
    override fun addProductPicture(productId: Long, picture: String): ProductDto {
        var product = productRepository.findById(productId)
        product?.get().pictureURL = picture
        return productRepository.save(product).convert()
    }



}