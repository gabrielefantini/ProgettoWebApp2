package it.polito.wa2.group17.order.controller

import it.polito.wa2.group17.common.utils.extractErrors
import it.polito.wa2.group17.order.model.OrderPatchRequest
import it.polito.wa2.group17.order.model.OrderRequest
import it.polito.wa2.group17.order.service.OrderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping(
    value = ["/orders"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class OrderController {

    @Autowired
    private lateinit var orderService: OrderService

    //GET
    @GetMapping
    fun getOrders() = ResponseEntity.ok(orderService.getOrders())

    @GetMapping("/{orderId}")
    fun getOrder(@PathVariable orderId: Long) = ResponseEntity.ok(orderService.getOrder(orderId))

    //POST
    @PostMapping
    fun addOrder(
        @RequestBody @Valid orderReq: OrderRequest,
        bindingResult: BindingResult
    ): ResponseEntity<*> {
        if(bindingResult.hasErrors())
            return ResponseEntity.badRequest().body(bindingResult.extractErrors())
        return ResponseEntity.ok(orderService.addOrder(orderReq))
    }

    //PATCH
    @PatchMapping("/{orderId}")
    fun updateOrder(
        @PathVariable orderId: Long,
        @RequestBody @Valid orderReq: OrderPatchRequest,
    ) = ResponseEntity.ok(orderService.updateOrder(orderId, orderReq).newOrder)


    //DELETE
    @DeleteMapping("/{orderId}")
    fun deleteOrder(@PathVariable orderId: Long) =
        ResponseEntity.ok(orderService.deleteOrder(orderId))
}