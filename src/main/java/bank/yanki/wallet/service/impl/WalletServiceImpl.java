package bank.yanki.wallet.service.impl;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import bank.yanki.wallet.dto.DebitCardDTO;
import bank.yanki.wallet.dto.MainAccountBalanceDTO;
import bank.yanki.wallet.event.WalletEvent;
import bank.yanki.wallet.model.Wallet;
import bank.yanki.wallet.producer.WalletEventProducer;
import bank.yanki.wallet.repository.WalletRepository;
import bank.yanki.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class WalletServiceImpl implements WalletService{
	
	private final WalletRepository walletRepository;
    private final WalletEventProducer eventProducer;
    private final KafkaTemplate<String, WalletEvent> kafkaTemplate;
    private final WebClient.Builder webClientBuilder;
    
	@Override
	public Mono<Wallet> createWallet(Wallet wallet) {
		return walletRepository.save(wallet)
                .doOnSuccess(savedWallet -> {
                    WalletEvent event = new WalletEvent();
                    event.setEventType("WALLET_CREATED");
                    event.setWallet(savedWallet);
                    eventProducer.sendWalletEvent(event);
                });
	}
	
	@Override
	public Mono<Wallet> sendMoney(String fromPhoneNumber, String toPhoneNumber, BigDecimal amount) {
		return walletRepository.findByPhoneNumber(toPhoneNumber) // Validar receptor
		        .switchIfEmpty(Mono.error(new RuntimeException("Receiver wallet not found")))
		        .flatMap(receiverWallet -> walletRepository.findByPhoneNumber(fromPhoneNumber)
		            .flatMap(senderWallet -> {
		                if (senderWallet.getBalance().compareTo(amount) < 0) {
		                    return Mono.error(new RuntimeException("Insufficient funds"));
		                }

		                senderWallet.setBalance(senderWallet.getBalance().subtract(amount));

		                return walletRepository.save(senderWallet)
		                    .doOnSuccess(updatedSender -> {
		                        WalletEvent sentEvent = WalletEvent.builder()
		                            .eventType("MONEY_SENT")
		                            .fromPhoneNumber(fromPhoneNumber)
		                            .toPhoneNumber(toPhoneNumber)
		                            .amount(amount)
		                            .build();
		                        eventProducer.sendWalletEvent(sentEvent);
		                    })
		                    .then(receiveMoney(toPhoneNumber, amount)); // usar método existente
		            })
		        );
	}	
	
	@Override
	public Mono<Wallet> receiveMoney(String phoneNumber, BigDecimal amount) {
		return walletRepository.findByPhoneNumber(phoneNumber)
	            .flatMap(receiverWallet -> {
	                receiverWallet.setBalance(receiverWallet.getBalance().add(amount));
	                return walletRepository.save(receiverWallet)
	                    .doOnSuccess(updatedReceiver -> {
	                        WalletEvent receiveEvent = new WalletEvent();
	                        receiveEvent.setEventType("MONEY_RECEIVED");
	                        receiveEvent.setToPhoneNumber(phoneNumber);
	                        receiveEvent.setAmount(amount);
	                        eventProducer.sendWalletEvent(receiveEvent);
	                    });
	            });
	}
	
	@Override
	public Mono<Wallet> loadFromDebitCard(String phoneNumber, BigDecimal amount) {
		return walletRepository.findByPhoneNumber(phoneNumber)
	            .flatMap(wallet -> {
	                wallet.setBalance(wallet.getBalance().add(amount));
	                return walletRepository.save(wallet)
	                    .doOnSuccess(updatedWallet -> {
	                        WalletEvent loadEvent = new WalletEvent();
	                        loadEvent.setEventType("LOAD_FROM_CARD");
	                        loadEvent.setFromPhoneNumber(phoneNumber);
	                        loadEvent.setAmount(amount);
	                        loadEvent.setDebitCardNumber(wallet.getLinkedDebitCardNumber());
	                        eventProducer.sendWalletEvent(loadEvent);
	                    });
	            });
	}
	@Override
	public Mono<Wallet> linkDebitCard(String phoneNumber, String cardNumber) {
		WebClient webClient = webClientBuilder.build(); 

        return walletRepository.findByPhoneNumber(phoneNumber)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found")))
            .flatMap(wallet -> {
                return webClient.get()
                    .uri("http://localhost:8021/debit-cards/{cardNumber}", cardNumber)
                    .retrieve()
                    .bodyToMono(DebitCardDTO.class)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Debit card not found")))
                    .flatMap(debitCard -> {
                        if (debitCard.getAccountNumbers() == null || debitCard.getAccountNumbers().isEmpty()) {
                            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debit card has no linked accounts"));
                        }

                        wallet.setLinkedDebitCardNumber(cardNumber);
                        return walletRepository.save(wallet)
                            .doOnSuccess(updated -> {
                            	WalletEvent event = WalletEvent.builder()
                            		    .eventType("WALLET_UPDATED")
                            		    .wallet(updated)
                            		    .build();
                                kafkaTemplate.send("wallet-events", event);
                            });
                    });
            });
	}

	@Override
	public Mono<Wallet> loadFromCard(String phoneNumber, BigDecimal amount) {
		WebClient webClient = webClientBuilder.build();

	    return walletRepository.findByPhoneNumber(phoneNumber)
	            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found")))
	            .flatMap(wallet -> {
	                String debitCardNumber = wallet.getLinkedDebitCardNumber();
	                System.out.println("debitCardNumber -- "+debitCardNumber);
	                if (debitCardNumber == null) {
	                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wallet has no linked debit card"));
	                }

	                return webClient.get()//Validar saldo suficiente
	                        .uri("http://localhost:8021/debit-cards/{cardNumber}/main-account/balance", debitCardNumber)
	                        .retrieve()
	                        .bodyToMono(MainAccountBalanceDTO.class)
	                        .flatMap(balanceDTO -> {
	                            if (balanceDTO.getBalance().compareTo(amount) < 0) {
	                                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance"));
	                            }
	                            
	                            // Enviar evento de carga a Kafka
	                            WalletEvent event = WalletEvent.builder()
	                            	    .eventType("LOAD_FROM_CARD")
	                            	    .fromPhoneNumber(wallet.getPhoneNumber())
	                            	    .debitCardNumber(wallet.getLinkedDebitCardNumber())
	                            	    .amount(amount)
	                            	    .build();

	                            // Aquí enviamos el evento (sin alterar el flujo)
	                            kafkaTemplate.send("wallet-events", event);

	                            // Luego simplemente retornamos el Wallet
	                            return Mono.just(wallet);
	                        });
	            });
	}


}
