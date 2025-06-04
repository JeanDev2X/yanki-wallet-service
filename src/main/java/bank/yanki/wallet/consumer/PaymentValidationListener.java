package bank.yanki.wallet.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import bank.yanki.wallet.dto.PaymentValidationRequestDTO;
import bank.yanki.wallet.dto.PaymentValidationResponseDTO;
import bank.yanki.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentValidationListener {
	
	private final WalletRepository walletRepository;
    private final KafkaTemplate<String, PaymentValidationResponseDTO> kafkaTemplate;
    
    @KafkaListener(
	    topics = "payment-validation-request",
	    groupId = "yanki-wallet-group",
	    containerFactory = "paymentValidationKafkaListenerContainerFactory"
	)
	public void listen(PaymentValidationRequestDTO request) {
        log.info("YANKI Recibido evento de validación de pago: {}", request);

        if (!"YANKI".equalsIgnoreCase(request.getPaymentMode())) {
            log.info("No es modo YANKI, ignorando en yanki-service");
            return;
        }

        walletRepository.findByPhoneNumber(request.getActorIdentifier())
            .map(wallet -> {
                boolean isValid = wallet.getBalance().compareTo(request.getAmountInSoles()) >= 0;
                return PaymentValidationResponseDTO.builder()
                        .transactionRef(request.getTransactionRef())
                        .valid(isValid)
                        .reason(isValid ? "Validación exitosa (Yanki)" : "Saldo insuficiente en Yanki")
                        .build();
            })
            .defaultIfEmpty(PaymentValidationResponseDTO.builder()
                    .transactionRef(request.getTransactionRef())
                    .valid(false)
                    .reason("Monedero no encontrado")
                    .build())
            .doOnNext(response -> {
                log.info("Publicando respuesta de validación: {}", response);
                kafkaTemplate.send("payment-validation-response", response);
            })
            .subscribe();
    }
    
}
