package id.ac.ui.cs.advprog.jsonpaymentservice.dto;

public record WalletMinimalResponse (
  String wallet_id,
  String user_id, 
  long balance
) {
}