package id.ac.ui.cs.advprog.jsonpaymentservice.repository;

import id.ac.ui.cs.advprog.jsonpaymentservice.model.Transaction;
import id.ac.ui.cs.advprog.jsonpaymentservice.model.enums.TransactionEnums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
	Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
	List<Transaction> findAllByStatus(TransactionEnums.Status status);
	List<Transaction> findAllByUserId(String userId);
	Optional<Transaction> findByReferenceIdAndType(String referenceId, TransactionEnums.Type type);
}
