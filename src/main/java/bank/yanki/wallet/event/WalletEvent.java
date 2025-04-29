package bank.yanki.wallet.event;

import java.math.BigDecimal;

import bank.yanki.wallet.model.Wallet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WalletEvent {
	private String eventType; // Ej: "WALLET_CREATED", "MONEY_SENT", "MONEY_RECEIVED", "LOAD_FROM_CARD"
    private Wallet wallet;            // Solo se usa en "WALLET_CREATED"
    private String fromPhoneNumber;   // De quién envía dinero
    private String toPhoneNumber;     // A quién recibe dinero
    private BigDecimal amount;        // Monto de dinero
    private String debitCardNumber;   // Para cargas desde tarjeta de débito
}
