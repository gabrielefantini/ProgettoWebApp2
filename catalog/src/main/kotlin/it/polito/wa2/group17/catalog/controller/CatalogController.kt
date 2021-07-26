package it.polito.wa2.group17.catalog.controller

import it.polito.wa2.group17.catalog.security.OnlyEnabledUsers
import it.polito.wa2.group17.catalog.service.CatalogService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping (
    value = ["/catalog"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class CatalogController {
    @Autowired
    private lateinit var catalogService: CatalogService

    @GetMapping ("/orders/{email}")
    @OnlyEnabledUsers
    fun getOrdersByEmail(@PathVariable email: String): ResponseEntity<Unit>/*OrderDto*/ {
        return ResponseEntity.ok(catalogService.getOrders(email))
    }

    @GetMapping ("/order/{orderId}")
    @OnlyEnabledUsers
    fun getOrderById(@PathVariable orderId: Long): ResponseEntity<Unit>/*OrderDto*/ {
        return ResponseEntity.ok(catalogService.getOrderById(orderId))
    }


}
