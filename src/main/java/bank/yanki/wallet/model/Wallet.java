package bank.yanki.wallet.model;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "wallets")
public class Wallet {
	@Id
    private String id;
    private String documentType;
    private String documentNumber;
    private String phoneNumber;
    private String imei;
    private String email;
    private BigDecimal balance = BigDecimal.ZERO;
    private String linkedDebitCardNumber; // opcional
}
