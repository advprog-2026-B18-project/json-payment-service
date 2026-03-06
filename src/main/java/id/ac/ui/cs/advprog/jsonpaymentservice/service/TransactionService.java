package id.ac.ui.cs.advprog.jsonpaymentservice.service;

import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.ConfirmTopUpResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.TransactionResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.request.TopUpRequest;
import id.ac.ui.cs.advprog.jsonpaymentservice.model.Transaction;
import id.ac.ui.cs.advprog.jsonpaymentservice.model.Wallet;
import id.ac.ui.cs.advprog.jsonpaymentservice.model.enums.TransactionEnums;
import id.ac.ui.cs.advprog.jsonpaymentservice.repository.TransactionRepository;
import id.ac.ui.cs.advprog.jsonpaymentservice.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TransactionService {
    private static final long MINIMUM_TOP_UP = 10_000L;

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    public TransactionService(TransactionRepository transactionRepository, WalletRepository walletRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
    }

    public TransactionResponse processRequestTopUp(TopUpRequest topUpRequest, String userId) {
        Long amount = topUpRequest.getAmount();
        if (amount == null || amount < MINIMUM_TOP_UP) {
            throw new MinimumTopUpException();
        }

        String idempotencyKey = topUpRequest.getIdempotencyKey();
        if (idempotencyKey != null) {
            Transaction existing = transactionRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
            if (existing != null) {
                throw new DuplicateRequestException(existing.getTransactionId());
            }
        }

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + userId));

        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setWalletId(wallet.getWalletId());
        transaction.setUserId(userId);
        transaction.setType(TransactionEnums.Type.TOPUP);
        transaction.setDirection(TransactionEnums.Direction.CREDIT);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionEnums.Status.PENDING);
        transaction.setDescription("Top up request");
        transaction.setBalanceBefore(wallet.getBalance() == null ? 0L : wallet.getBalance());
        transaction.setPaymentMethod(topUpRequest.getPaymentMethod());
        transaction.setPaymentReference(topUpRequest.getBankCode());
        transaction.setIdempotencyKey(idempotencyKey);

        Transaction saved = transactionRepository.save(transaction);

        return new TransactionResponse(
                saved.getTransactionId(),
                saved.getType().name(),
                saved.getAmount(),
                saved.getStatus().name(),
                saved.getCreatedAt()
        );
    }

        public ConfirmTopUpResponse confirmTopUp(String transactionId, String adminUserId) {
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));

        if (transaction.getStatus() != TransactionEnums.Status.PENDING) {
            throw new TransactionHasBeenConfirmedException();
        }

        Wallet wallet = walletRepository.findById(transaction.getWalletId())
            .orElseThrow(() -> new RuntimeException("Wallet not found: " + transaction.getWalletId()));

        Long currentBalance = wallet.getBalance() == null ? 0L : wallet.getBalance();
        Long newBalance = currentBalance + transaction.getAmount();

        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        transaction.setStatus(TransactionEnums.Status.SUCCESS);
        transaction.setBalanceAfter(newBalance);
        transaction.setConfirmedBy(adminUserId);

        Transaction savedTransaction = transactionRepository.save(transaction);

        return new ConfirmTopUpResponse(
            savedTransaction.getTransactionId(),
            savedTransaction.getStatus().name(),
            savedTransaction.getAmount(),
            newBalance,
            savedTransaction.getUpdatedAt()
        );
        }

    public static class MinimumTopUpException extends RuntimeException {
        public MinimumTopUpException() {
            super("Minimum top-up is Rp 10.000");
        }
    }

    public static class DuplicateRequestException extends RuntimeException {
        private final String existingTransactionId;

        public DuplicateRequestException(String existingTransactionId) {
            super("Duplicate request");
            this.existingTransactionId = existingTransactionId;
        }

        public String getExistingTransactionId() {
            return existingTransactionId;
        }
    }

    public static class TransactionHasBeenConfirmedException extends RuntimeException {
        public TransactionHasBeenConfirmedException() {
            super("Transaction has been confirmed");
        }
    }
}
