package bank.yanki.wallet.dto;

import lombok.Data;

@Data
public class WalletRequestDTO {
	private String documentType;
    private String documentNumber;
    private String phoneNumber;
    private String imei;
    private String email;
}
