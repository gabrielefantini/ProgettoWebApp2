package it.polito.wa2.group17.catalog.connector

import it.polito.wa2.group17.common.connector.Connector
import it.polito.wa2.group17.common.dto.OrderPatchRequest
import it.polito.wa2.group17.common.dto.PutProductRequest
import it.polito.wa2.group17.common.dto.RatingDto
import it.polito.wa2.group17.common.dto.RatingRequest
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Primary

@Connector
@Primary
@ConditionalOnProperty(prefix = "connectors.product.mock", name = ["enabled"], havingValue = "true")

class ProductConnectorMocked: ProductConnector() {
    override fun rateProductById(productId: Long, ratingDto: RatingRequest) = 0L

    override fun addProduct(productId: Long, putProductRequest: PutProductRequest) = productId

    override fun deleteProduct(productId: Long) = productId
}
