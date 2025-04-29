package bank.yanki.wallet.service;

import java.math.BigDecimal;

import bank.yanki.wallet.dto.WalletRequestDTO;
import bank.yanki.wallet.model.Wallet;
import reactor.core.publisher.Mono;

public interface WalletService {
	Mono<Wallet> createWallet(Wallet wallet);
	Mono<Wallet> sendMoney(String fromPhoneNumber, String toPhoneNumber, BigDecimal amount);
	Mono<Wallet> receiveMoney(String phoneNumber, BigDecimal amount);
	Mono<Wallet> loadFromDebitCard(String phoneNumber, BigDecimal amount);
	Mono<Wallet> linkDebitCard(String phoneNumber, String cardNumber);
	Mono<Wallet> loadFromCard(String phoneNumber, BigDecimal amount);
}
