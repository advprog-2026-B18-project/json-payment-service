package id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TopUpRequest {
    private Long amount;

    @JsonProperty("payment_method")
    private String paymentMethod;

    @JsonProperty("bank_code")
    private String bankCode;

    @JsonProperty("idempotency_key")
    private String idempotencyKey;
}
