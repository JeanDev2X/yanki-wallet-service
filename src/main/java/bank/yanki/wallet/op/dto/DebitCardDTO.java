package bank.yanki.wallet.op.dto;

import java.util.List;

import lombok.Data;

@Data
public class DebitCardDTO {
	private String cardNumber;
    private List<String> accountNumbers;
}
