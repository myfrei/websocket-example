package com.bankdemo.account.repo;

import com.bankdemo.account.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findTop50ByOwnerUsernameOrderByCreatedAtDesc(String ownerUsername);
}
