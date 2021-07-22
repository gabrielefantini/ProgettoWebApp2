package it.polito.wa2.group17.wallet.controller

import it.polito.wa2.group17.common.utils.extractErrors
import it.polito.wa2.group17.wallet.model.Transaction
import it.polito.wa2.group17.wallet.model.TransactionRequest
import it.polito.wa2.group17.wallet.model.Wallet
import it.polito.wa2.group17.wallet.service.WalletService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.time.Instant
import javax.validation.Valid

@RestController
@RequestMapping(
    value = ["/wallets"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class WalletController {

    @Autowired
    private lateinit var walletService: WalletService


    @PostMapping
    fun addWalletToUser(@RequestBody userID: Long): ResponseEntity<Wallet> {
        val wallet = walletService.addWalletToUser(userID)
        return ResponseEntity.created(URI.create("/wallet/${wallet.id}")).body(wallet)
    }

    @GetMapping("/{walletID}")
    fun getWalletById(@PathVariable walletID: Long): ResponseEntity<Wallet> =
        ResponseEntity.ok(walletService.getWallet(walletID))

    @PostMapping("/{walletID}/transactions")
    fun performTransaction(
        @PathVariable walletID: Long,
        @RequestBody @Valid transactionRequest: TransactionRequest,
        bindingResult: BindingResult
    ): ResponseEntity<*> {
        if (bindingResult.hasErrors())
            return ResponseEntity.badRequest().body(bindingResult.extractErrors())

        val transaction = walletService.performTransaction(
            transactionRequest.amount,
            transactionRequest.reason,
            walletID,
            transactionRequest.userId,
            transactionRequest.timeInstant
        )
        return ResponseEntity
            .created(URI.create("/wallet/$walletID/transactions/${transaction.id}"))
            .body(transaction)

    }

    @GetMapping("/{walletId}/transactions")
    fun getTransactionsOfWallet(
        @PathVariable walletId: Long,
        @RequestParam("from", required = false) from: Instant,
        @RequestParam("to", required = false) to: Instant
    ): ResponseEntity<List<Transaction>> =
        ResponseEntity.ok(
            walletService
                .getTransactionsOfWallet(walletId, from, to)
        )


    @GetMapping("{walletId}/transactions/{transactionId}")
    fun getTransactionOfWallet(
        @PathVariable("walletId") walletId: Long,
        @PathVariable("transactionId") transactionId: Long
    ): ResponseEntity<Transaction> =
        ResponseEntity.ok(walletService.getTransactionOfWallet(walletId, transactionId))


}
