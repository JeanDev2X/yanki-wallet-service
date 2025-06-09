package bank.yanki.wallet.op.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SendMoneyRequestDTO {
	private String senderPhoneNumber;
    private String receiverPhoneNumber;
    private BigDecimal amount;
}
