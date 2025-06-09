package bank.yanki.wallet.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import com.bootcoin.dto.PaymentExecutionRequestDTO;
import com.bootcoin.dto.PaymentValidationRequestDTO;
import bank.yanki.wallet.event.WalletEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {
	
	@Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
	
	@Bean
    public ConsumerFactory<String, WalletEvent> consumerFactory() {
        JsonDeserializer<WalletEvent> deserializer = new JsonDeserializer<>(WalletEvent.class, false);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "wallet-events-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

	@Bean(name = "walletEventKafkaListenerContainerFactory")
	public ConcurrentKafkaListenerContainerFactory<String, WalletEvent> kafkaListenerContainerFactory() {
	    ConcurrentKafkaListenerContainerFactory<String, WalletEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
	    factory.setConsumerFactory(consumerFactory());
	    return factory;
	}
    
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, PaymentValidationRequestDTO> paymentValidationKafkaListenerContainerFactory(
	        ConsumerFactory<String, PaymentValidationRequestDTO> consumerFactory) {
	    ConcurrentKafkaListenerContainerFactory<String, PaymentValidationRequestDTO> factory =
	            new ConcurrentKafkaListenerContainerFactory<>();
	    factory.setConsumerFactory(consumerFactory);
	    return factory;
	}
	
	@Bean
	public ConsumerFactory<String, PaymentValidationRequestDTO> paymentValidationConsumerFactory() {
	    Map<String, Object> props = new HashMap<>();
	    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
	    props.put(ConsumerConfig.GROUP_ID_CONFIG, "yanki-wallet-group");
	    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
	    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
	    props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
	    props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.bootcoin.dto.PaymentValidationRequestDTO");
	    return new DefaultKafkaConsumerFactory<>(props,
	            new StringDeserializer(),
	            new JsonDeserializer<>(PaymentValidationRequestDTO.class, false));
	}
	
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, PaymentExecutionRequestDTO> paymentExecutionKafkaListenerContainerFactory() {
	    ConcurrentKafkaListenerContainerFactory<String, PaymentExecutionRequestDTO> factory =
	            new ConcurrentKafkaListenerContainerFactory<>();
	    factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(
	            Map.of(
	                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
	                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
	                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class,
	                JsonDeserializer.TRUSTED_PACKAGES, "*"
	            )
	    ));
	    return factory;
	}
	
}
