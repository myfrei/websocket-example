package com.bankdemo.account.repo;

import com.bankdemo.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByAccountNumber(String accountNumber);

    Optional<Account> findFirstByPhoneOrAccountNumber(String phone, String accountNumber);
}
