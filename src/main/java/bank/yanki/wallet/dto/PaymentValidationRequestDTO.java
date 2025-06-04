package bank.yanki.wallet.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentValidationRequestDTO {
	private String transactionRef;
    private String actorIdentifier; // vendedor (SELL) o comprador (BUY)
    private String paymentMode; // YANKI o TRANSFER
    private String transactionType; // BUY o SELL
    private BigDecimal amountInSoles;
    private String phoneNumber; // para transferencia bancaria (no se usa aquí)
    private String accountNumber; // para transferencia bancaria (no se usa aquí)
}
