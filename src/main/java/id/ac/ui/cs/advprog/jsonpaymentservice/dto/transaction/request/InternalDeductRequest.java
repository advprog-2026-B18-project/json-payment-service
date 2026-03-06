package id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InternalDeductRequest {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("order_id")
    private String orderId;

    private Long amount;

    private String description;
}