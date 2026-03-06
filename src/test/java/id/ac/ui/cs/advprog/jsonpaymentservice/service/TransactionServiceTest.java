package id.ac.ui.cs.advprog.jsonpaymentservice.service;

import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.ConfirmTopUpResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.DeductPaymentResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.PendingTransactionResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.TransactionResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.UserTopUpTransactionResponse;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.request.InternalDeductRequest;
import id.ac.ui.cs.advprog.jsonpaymentservice.dto.transaction.request.TopUpRequest;
import id.ac.ui.cs.advprog.jsonpaymentservice.model.Transaction;
import id.ac.ui.cs.advprog.jsonpaymentservice.model.Wallet;
import id.ac.ui.cs.advprog.jsonpaymentservice.model.enums.TransactionEnums;
import id.ac.ui.cs.advprog.jsonpaymentservice.repository.TransactionRepository;
import id.ac.ui.cs.advprog.jsonpaymentservice.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void processRequestTopUpSuccessCreatesPendingTransaction() {
        String userId = "user-123";
        TopUpRequest request = new TopUpRequest();
        request.setAmount(10000L);
        request.setPaymentMethod("VA");
        request.setBankCode("BCA");
        request.setIdempotencyKey("idem-1");

        Wallet wallet = new Wallet();
        wallet.setWalletId("wallet-123");
        wallet.setUserId(userId);
        wallet.setBalance(25000L);

        when(transactionRepository.findByIdempotencyKey("idem-1")).thenReturn(Optional.empty());
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction toSave = invocation.getArgument(0);
            toSave.setCreatedAt(LocalDateTime.now());
            return toSave;
        });

        TransactionResponse response = transactionService.processRequestTopUp(request, userId);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        Transaction saved = captor.getValue();

        assertEquals(TransactionEnums.Type.TOPUP, saved.getType());
        assertEquals(TransactionEnums.Status.PENDING, saved.getStatus());
        assertEquals(TransactionEnums.Direction.CREDIT, saved.getDirection());
        assertEquals(10000L, saved.getAmount());
        assertEquals("VA", saved.getPaymentMethod());
        assertEquals("BCA", saved.getPaymentReference());
        assertEquals("idem-1", saved.getIdempotencyKey());
        assertEquals(25000L, saved.getBalanceBefore());

        assertEquals("TOPUP", response.type());
        assertEquals(10000L, response.amount());
        assertEquals("PENDING", response.status());
        assertTrue(response.transaction_id() != null && !response.transaction_id().isBlank());
    }

    @Test
    void processRequestTopUpAmountUnderMinimumThrowsMinimumTopUpException() {
        TopUpRequest request = new TopUpRequest();
        request.setAmount(9999L);

        assertThrows(TransactionService.MinimumTopUpException.class,
                () -> transactionService.processRequestTopUp(request, "user-123"));

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void processRequestTopUpWithNullIdempotencyKeySuccess() {
        String userId = "user-123";
        TopUpRequest request = new TopUpRequest();
        request.setAmount(20000L);
        request.setPaymentMethod("VA");
        request.setBankCode("BCA");
        request.setIdempotencyKey(null);

        Wallet wallet = new Wallet();
        wallet.setWalletId("wallet-123");
        wallet.setUserId(userId);
        wallet.setBalance(100000L);

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction toSave = invocation.getArgument(0);
            toSave.setCreatedAt(LocalDateTime.now());
            return toSave;
        });

        TransactionResponse response = transactionService.processRequestTopUp(request, userId);

        assertEquals("TOPUP", response.type());
        assertEquals(20000L, response.amount());
    }

    @Test
    void processRequestTopUpDuplicateIdempotencyKeyThrowsDuplicateRequestException() {
        TopUpRequest request = new TopUpRequest();
        request.setAmount(15000L);
        request.setIdempotencyKey("dup-1");

        Transaction existing = new Transaction();
        existing.setTransactionId("existing-tx-id");

        when(transactionRepository.findByIdempotencyKey("dup-1")).thenReturn(Optional.of(existing));

        TransactionService.DuplicateRequestException exception = assertThrows(
                TransactionService.DuplicateRequestException.class,
                () -> transactionService.processRequestTopUp(request, "user-123")
        );

        assertEquals("existing-tx-id", exception.getExistingTransactionId());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void processRequestTopUpWalletNotFoundThrowsRuntimeException() {
        String userId = "missing-user";
        TopUpRequest request = new TopUpRequest();
        request.setAmount(15000L);
        request.setIdempotencyKey("idem-9");

        when(transactionRepository.findByIdempotencyKey("idem-9")).thenReturn(Optional.empty());
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transactionService.processRequestTopUp(request, userId));

        assertEquals("Wallet not found for user: " + userId, exception.getMessage());
    }

    @Test
    void confirmTopUpSuccessMarksTransactionSuccessAndUpdatesWalletBalance() {
        String transactionId = "tx-123";
        String adminUserId = "admin-1";

        Transaction pendingTopUp = new Transaction();
        pendingTopUp.setTransactionId(transactionId);
        pendingTopUp.setWalletId("wallet-1");
        pendingTopUp.setAmount(10000L);
        pendingTopUp.setStatus(TransactionEnums.Status.PENDING);

        Wallet wallet = new Wallet();
        wallet.setWalletId("wallet-1");
        wallet.setBalance(25000L);

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(pendingTopUp));
        when(walletRepository.findById("wallet-1")).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setUpdatedAt(LocalDateTime.now());
            return t;
        });

        ConfirmTopUpResponse response = transactionService.confirmTopUp(transactionId, adminUserId);

        assertEquals("tx-123", response.transaction_id());
        assertEquals("SUCCESS", response.status());
        assertEquals(10000L, response.amount());
        assertEquals(35000L, response.new_balance());

        assertEquals(35000L, wallet.getBalance());
        assertEquals(TransactionEnums.Status.SUCCESS, pendingTopUp.getStatus());
        assertEquals(35000L, pendingTopUp.getBalanceAfter());
        assertEquals("admin-1", pendingTopUp.getConfirmedBy());
    }

    @Test
    void confirmTopUpAlreadyConfirmedThrowsTransactionHasBeenConfirmedException() {
        Transaction existing = new Transaction();
        existing.setTransactionId("tx-123");
        existing.setStatus(TransactionEnums.Status.SUCCESS);

        when(transactionRepository.findById("tx-123")).thenReturn(Optional.of(existing));

        assertThrows(TransactionService.TransactionHasBeenConfirmedException.class,
                () -> transactionService.confirmTopUp("tx-123", "admin-1"));

        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void confirmTopUpTransactionNotFoundThrowsRuntimeException() {
        when(transactionRepository.findById("missing-tx")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transactionService.confirmTopUp("missing-tx", "admin-1"));

        assertEquals("Transaction not found: missing-tx", exception.getMessage());
    }

    @Test
    void confirmTopUpWalletNotFoundThrowsRuntimeException() {
        Transaction pendingTopUp = new Transaction();
        pendingTopUp.setTransactionId("tx-123");
        pendingTopUp.setWalletId("wallet-missing");
        pendingTopUp.setAmount(10000L);
        pendingTopUp.setStatus(TransactionEnums.Status.PENDING);

        when(transactionRepository.findById("tx-123")).thenReturn(Optional.of(pendingTopUp));
        when(walletRepository.findById("wallet-missing")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transactionService.confirmTopUp("tx-123", "admin-1"));

        assertEquals("Wallet not found: wallet-missing", exception.getMessage());
    }

    @Test
    void getPendingTransactionsReturnsAllPendingWithTransactionIdAndAmount() {
        Transaction first = new Transaction();
        first.setTransactionId("tx-1");
        first.setAmount(10000L);
        first.setCreatedAt(LocalDateTime.now());

        Transaction second = new Transaction();
        second.setTransactionId("tx-2");
        second.setAmount(25000L);
        second.setCreatedAt(LocalDateTime.now());

        when(transactionRepository.findAllByStatus(TransactionEnums.Status.PENDING))
                .thenReturn(List.of(first, second));

        List<PendingTransactionResponse> responses = transactionService.getPendingTransactions();

        assertEquals(2, responses.size());
        assertEquals("tx-1", responses.get(0).transaction_id());
        assertEquals(10000L, responses.get(0).amount());
        assertTrue(responses.get(0).created_at() != null);
        assertEquals("tx-2", responses.get(1).transaction_id());
        assertEquals(25000L, responses.get(1).amount());
        assertTrue(responses.get(1).created_at() != null);
    }

    @Test
    void getCurrentUserTopUpTransactionsReturnsAllCurrentUserTransactions() {
        String userId = "user-123";

        Transaction first = new Transaction();
        first.setTransactionId("tx-1");
        first.setAmount(10000L);
        first.setStatus(TransactionEnums.Status.PENDING);
        first.setCreatedAt(LocalDateTime.now());

        Transaction second = new Transaction();
        second.setTransactionId("tx-2");
        second.setAmount(15000L);
        second.setStatus(TransactionEnums.Status.SUCCESS);
        second.setCreatedAt(LocalDateTime.now());

        when(transactionRepository.findAllByUserId(userId)).thenReturn(List.of(first, second));

        List<UserTopUpTransactionResponse> responses = transactionService.getCurrentUserTopUpTransactions(userId);

        assertEquals(2, responses.size());
        assertEquals("tx-1", responses.get(0).transaction_id());
        assertEquals(10000L, responses.get(0).amount());
        assertEquals("PENDING", responses.get(0).status());
        assertTrue(responses.get(0).created_at() != null);

        assertEquals("tx-2", responses.get(1).transaction_id());
        assertEquals(15000L, responses.get(1).amount());
        assertEquals("SUCCESS", responses.get(1).status());
        assertTrue(responses.get(1).created_at() != null);
    }

        @Test
        void processInternalDeductSuccessMovesFundsToEscrowAndCreatesPaymentTransaction() {
        InternalDeductRequest request = new InternalDeductRequest();
        request.setUserId("user-1");
        request.setOrderId("order-1");
        request.setAmount(150000L);
        request.setDescription("Order payment");

        Wallet wallet = new Wallet();
        wallet.setWalletId("wallet-1");
        wallet.setUserId("user-1");
        wallet.setBalance(200000L);
        wallet.setEscrowBalance(20000L);

        when(transactionRepository.findByReferenceIdAndType("order-1", TransactionEnums.Type.PAYMENT))
            .thenReturn(Optional.empty());
        when(walletRepository.findByUserId("user-1")).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeductPaymentResponse response = transactionService.processInternalDeduct(request);

        assertNotNull(response.transaction_id());
        assertEquals("PAYMENT", response.type());
        assertEquals("user-1", response.user_id());
        assertEquals(150000L, response.amount());
        assertEquals(50000L, response.new_balance());
        assertEquals(170000L, response.escrow_balance());
        assertEquals("SUCCESS", response.status());
        }

        @Test
        void processInternalDeductDuplicateOrderThrowsPaymentAlreadyProcessedException() {
        InternalDeductRequest request = new InternalDeductRequest();
        request.setUserId("user-1");
        request.setOrderId("order-1");
        request.setAmount(10000L);

        Transaction existing = new Transaction();
        existing.setTransactionId("tx-existing");

        when(transactionRepository.findByReferenceIdAndType("order-1", TransactionEnums.Type.PAYMENT))
            .thenReturn(Optional.of(existing));

        TransactionService.PaymentAlreadyProcessedException exception = assertThrows(
            TransactionService.PaymentAlreadyProcessedException.class,
            () -> transactionService.processInternalDeduct(request)
        );

        assertEquals("tx-existing", exception.getTransactionId());
        }

        @Test
        void processInternalDeductInsufficientBalanceThrowsInsufficientBalanceException() {
        InternalDeductRequest request = new InternalDeductRequest();
        request.setUserId("user-1");
        request.setOrderId("order-1");
        request.setAmount(150000L);

        Wallet wallet = new Wallet();
        wallet.setWalletId("wallet-1");
        wallet.setUserId("user-1");
        wallet.setBalance(100000L);
        wallet.setEscrowBalance(20000L);

        when(transactionRepository.findByReferenceIdAndType("order-1", TransactionEnums.Type.PAYMENT))
            .thenReturn(Optional.empty());
        when(walletRepository.findByUserId("user-1")).thenReturn(Optional.of(wallet));

        TransactionService.InsufficientBalanceException exception = assertThrows(
            TransactionService.InsufficientBalanceException.class,
            () -> transactionService.processInternalDeduct(request)
        );

        assertEquals(80000L, exception.getBalance());
        assertEquals(150000L, exception.getRequired());
        }

        @Test
        void processInternalDeductUserNotFoundThrowsUserNotFoundException() {
        InternalDeductRequest request = new InternalDeductRequest();
        request.setUserId("missing-user");
        request.setOrderId("order-1");
        request.setAmount(10000L);

        when(transactionRepository.findByReferenceIdAndType("order-1", TransactionEnums.Type.PAYMENT))
            .thenReturn(Optional.empty());
        when(walletRepository.findByUserId("missing-user")).thenReturn(Optional.empty());

        assertThrows(TransactionService.UserNotFoundException.class,
            () -> transactionService.processInternalDeduct(request));
        }
}
