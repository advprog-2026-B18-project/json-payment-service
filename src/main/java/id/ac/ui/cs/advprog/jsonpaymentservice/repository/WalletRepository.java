package id.ac.ui.cs.advprog.jsonpaymentservice.repository;

import id.ac.ui.cs.advprog.jsonpaymentservice.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, String> {

    Optional<Wallet> findByUserId(String userId);
}