package bank.yanki.wallet.controller;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import bank.yanki.wallet.model.Wallet;
import bank.yanki.wallet.repository.WalletRepository;
import bank.yanki.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {
	private final WalletService walletService;
	private final WalletRepository walletRepository;
	@PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Wallet> createWallet(@RequestBody Wallet wallet) {
        return walletService.createWallet(wallet);
    }
	//Enviar dinero de una wallet a otra
	@PostMapping("/send-money")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Wallet> sendMoney(
            @RequestParam String fromPhoneNumber,
            @RequestParam String toPhoneNumber,
            @RequestParam BigDecimal amount) {
        return walletService.sendMoney(fromPhoneNumber, toPhoneNumber, amount);
    }
	//Recibir dinero manualmente (solo prueba de receiveMoney)
	@PostMapping("/receive-money")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Wallet> receiveMoney(
            @RequestParam String phoneNumber,
            @RequestParam BigDecimal amount) {
        return walletService.receiveMoney(phoneNumber, amount);
    }
	//Cargar saldo simulando carga manual con tarjeta asociada
	@PostMapping("/load-from-debit-card")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Wallet> loadFromDebitCard(
            @RequestParam String phoneNumber,
            @RequestParam BigDecimal amount) {
        return walletService.loadFromDebitCard(phoneNumber, amount);
    }
	//Asociar una tarjeta de débito
	@PatchMapping("/{phoneNumber}/link-debit-card")
	public Mono<Wallet> linkDebitCard(
	        @PathVariable String phoneNumber,
	        @RequestParam String cardNumber) {
	    return walletService.linkDebitCard(phoneNumber, cardNumber);
	}
	//Cargar desde cuenta principal (carga automática)
	@PostMapping("/{phoneNumber}/load-from-card")
    public Mono<ResponseEntity<Wallet>> loadFromCard(
            @PathVariable String phoneNumber,
            @RequestParam BigDecimal amount) {

        return walletService.loadFromCard(phoneNumber, amount)
                .map(wallet -> ResponseEntity.ok(wallet))
                .onErrorResume(e -> Mono.just(ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .build()));
    }
	@GetMapping
	public Flux<Wallet> getAllWallets() {
	    return walletRepository.findAll();
	}
}
