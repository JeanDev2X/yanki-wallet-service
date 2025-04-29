package bank.yanki.wallet.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import bank.yanki.wallet.event.WalletEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WalletEventProducer {
	
	private final KafkaTemplate<String, WalletEvent> kafkaTemplate;
    private static final String TOPIC = "wallet-events";

    public void sendWalletEvent(WalletEvent event) {
        kafkaTemplate.send(TOPIC, event);
    }
}
