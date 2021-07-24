package it.polito.wa2.group17.warehouse.controller

import it.polito.wa2.group17.common.utils.extractErrors
import it.polito.wa2.group17.warehouse.dto.FulfillRequest
import it.polito.wa2.group17.warehouse.dto.SellRequest
import it.polito.wa2.group17.warehouse.dto.WarehouseRequest
import it.polito.wa2.group17.warehouse.service.WarehouseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import java.net.URI
import javax.validation.Valid


@RestController
@RequestMapping(
    value = ["/warehouses"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class WarehouseController {

    @Autowired
    private lateinit var warehouseService: WarehouseService

    @GetMapping
    fun getWarehouses() =
        ResponseEntity.ok(warehouseService.getWarehouses())

    @GetMapping("/{warehouseId}")
    fun getWarehouse(@PathVariable warehouseId: Long) =
        ResponseEntity.ok(warehouseService.getWarehouse(warehouseId))

    @PostMapping("/sell")
    fun sellProductFromAnyWarehouse(@RequestBody @Valid sellRequest: SellRequest) =
        ResponseEntity.ok(warehouseService.sellProduct(sellRequest))

    @PostMapping("/{warehouseId}/sell")
    fun sellProductFromWarehouse(
        @PathVariable warehouseId: Long,
        @RequestBody @Valid sellRequest: SellRequest
    ) =
        ResponseEntity.ok(warehouseService.sellProduct(sellRequest.apply { this.warehouseID = warehouseId }))

    @PostMapping("/{warehouseId}/fulfill")
    fun fulfillWarehouse(@PathVariable warehouseId: Long, @RequestBody @Valid fulfillRequest: FulfillRequest) =
        ResponseEntity.ok(warehouseService.fulfillProduct(fulfillRequest.apply { warehouseID = warehouseId }))

    @PostMapping
    fun saveWarehouse(
        @RequestBody @Valid warehouseRequest: WarehouseRequest,
        bindingResult: BindingResult
    ): ResponseEntity<*> {
        if (bindingResult.hasErrors())
            return ResponseEntity.badRequest().body(bindingResult.extractErrors())

        val warehouse = warehouseService.createWarehouse(warehouseRequest.products)
        return ResponseEntity
            .created(URI.create("/warehouses/${warehouse.id}"))
            .body(warehouse)
    }

    @PutMapping("/{warehouseId}")
    fun fullyUpdateOrInsertWarehouse(
        @PathVariable warehouseId: Long,
        @RequestBody @Valid warehouseRequest: WarehouseRequest
    ) = ResponseEntity.ok(warehouseService.updateOrInsertWarehouse(warehouseId, warehouseRequest.products))

    @PatchMapping("/{warehouseId}")
    fun partiallyUpdateWarehouse(
        @PathVariable warehouseId: Long,
        @RequestBody @Valid warehouseRequest: WarehouseRequest
    ) = ResponseEntity.ok(warehouseService.partiallyUpdateWarehouse(warehouseId, warehouseRequest.products))


    @DeleteMapping("/{warehouseId}")
    fun deleteWarehouse(@PathVariable warehouseId: Long) =
        ResponseEntity.ok(warehouseService.deleteWarehouse(warehouseId))

}
