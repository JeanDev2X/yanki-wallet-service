package bank.yanki.wallet.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.bootcoin.dto.PaymentExecutionRequestDTO;
import com.bootcoin.dto.PaymentExecutionResponseDTO;
import bank.yanki.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentExecutionListener {
	
	private final WalletRepository walletRepository;
    private final KafkaTemplate<String, PaymentExecutionResponseDTO> kafkaTemplate;

    @KafkaListener(
        topics = "payment-execution-request",
        groupId = "yanki-service",
        containerFactory = "paymentExecutionKafkaListenerContainerFactory"
    )
    public void handleExecutionRequest(PaymentExecutionRequestDTO request) {
        if (!"YANKI".equalsIgnoreCase(request.getPaymentMode())) {
            log.info("Modo de pago no es YANKI, se ignora: {}", request.getPaymentMode());
            return;
        }

        log.info("Procesando ejecución de pago YANKI: {}", request);
        walletRepository.findByPhoneNumber(request.getActorIdentifier())
            .flatMap(wallet -> {
                if (wallet.getBalance().compareTo(request.getAmountInSoles()) < 0) {
                    return publishResponse(request, false, "Saldo insuficiente");
                }
                wallet.setBalance(wallet.getBalance().subtract(request.getAmountInSoles()));
                return walletRepository.save(wallet)
                        .then(publishResponse(request, true, "Pago ejecutado"));
            })
            .switchIfEmpty(publishResponse(request, false, "Wallet no encontrado"))
            .subscribe();
    }

    private Mono<Void> publishResponse(PaymentExecutionRequestDTO request, boolean executed, String message) {
        PaymentExecutionResponseDTO response = PaymentExecutionResponseDTO.builder()
            .transactionRef(request.getTransactionRef())
            .executed(executed)
            .message(message)
            .build();

        log.info("Respuesta de ejecución: {}", response);
        kafkaTemplate.send("payment-execution-response", response);
        return Mono.empty();
    }
    
}
