package bank.yanki.wallet.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import bank.yanki.wallet.event.WalletEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WalletEventConsumer {
	@KafkaListener(topics = "wallet-events", groupId = "yanki-wallet-group", containerFactory = "walletEventKafkaListenerContainerFactory")
    public void consume(WalletEvent walletEvent) {
		log.info("Evento recibido desde Kafka: {}", walletEvent);

        // Aquí podrías reaccionar diferente dependiendo del tipo de evento si quieres
        switch (walletEvent.getEventType()) {
            case "WALLET_CREATED":
                log.info("Wallet created: {}", walletEvent.getWallet());
                break;
            case "MONEY_SENT":
                log.info("Money sent from {} to {} amount {}", walletEvent.getFromPhoneNumber(), walletEvent.getToPhoneNumber(), walletEvent.getAmount());
                break;
            case "MONEY_RECEIVED":
                log.info("Money received by {} amount {}", walletEvent.getToPhoneNumber(), walletEvent.getAmount());
                break;
            case "LOAD_FROM_CARD":
                log.info("Money loaded from debit card {} to phone number {} amount {}", walletEvent.getDebitCardNumber(), walletEvent.getFromPhoneNumber(), walletEvent.getAmount());
                break;
            default:
                log.warn("Unknown event type: {}", walletEvent.getEventType());
        }
    }
}
