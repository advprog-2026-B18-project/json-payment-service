package id.ac.ui.cs.advprog.jsonpaymentservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "wallets")
public class Wallet {

    @Id
    @Column(name = "wallet_id", nullable = false, length = 36)
    private String walletId;

    @Column(name = "user_id", nullable = false, length = 36, unique = true)
    private String userId;

    @Column(name = "balance", nullable = false)
    private Long balance = 0L;

    @Column(name = "escrow_balance", nullable = false)
    private Long escrowBalance = 0L;

    @Column(name = "currency", nullable = false)
    private String currency = "IDR";

    @Column(name = "total_topup_lifetime", nullable = false)
    private Long totalTopupLifetime = 0L;

    @Column(name = "total_withdrawal_lifetime", nullable = false)
    private Long totalWithdrawalLifetime = 0L;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}