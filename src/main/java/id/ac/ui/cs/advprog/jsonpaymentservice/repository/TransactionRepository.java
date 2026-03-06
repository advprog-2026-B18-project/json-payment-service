package id.ac.ui.cs.advprog.jsonpaymentservice.repository;

import id.ac.ui.cs.advprog.jsonpaymentservice.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
	Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
}
