package it.polito.wa2.group17.warehouse.service

import it.polito.wa2.group17.common.transaction.MultiserviceTransactional
import it.polito.wa2.group17.common.utils.converter.convert
import it.polito.wa2.group17.warehouse.dto.ProductDto
import it.polito.wa2.group17.warehouse.dto.PutProductRequest
import it.polito.wa2.group17.warehouse.repository.ProductRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

interface ProductService {
    abstract fun getProductsByCategory(category: String?): List<ProductDto>
    abstract fun getProductsById(id: Long): ProductDto
    abstract fun putProductById(productId: Long, product: PutProductRequest): ProductDto

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

    override fun getProductsById(productId: Long): ProductDto {
        logger.info("Getting product by Id")
        return productRepository
            .findById(productId)
            .convert()
    }

    //TODO riprendere da qua domani
    @MultiserviceTransactional
    override fun putProductById(productId: Long, product: PutProductRequest): ProductDto {
       logger.info("Putting product by Id")
    }
}