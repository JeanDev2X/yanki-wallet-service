package bank.yanki.wallet.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import bank.yanki.wallet.model.Wallet;
import reactor.core.publisher.Mono;

@Repository
public interface WalletRepository extends ReactiveMongoRepository<Wallet, String>												{
	Mono<Wallet> findByPhoneNumber(String phoneNumber);
}