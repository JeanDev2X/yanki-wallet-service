package bank.yanki.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentValidationResponseDTO {
	private String transactionRef;
    private boolean valid;
    private String reason;
}
