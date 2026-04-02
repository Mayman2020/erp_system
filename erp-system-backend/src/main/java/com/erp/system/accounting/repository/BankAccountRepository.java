package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.BankAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    @EntityGraph(attributePaths = {"linkedAccount"})
    List<BankAccount> findAllByOrderByBankNameAscAccountNumberAsc();

    @EntityGraph(attributePaths = {"linkedAccount"})
    Optional<BankAccount> findById(Long id);

    List<BankAccount> findByActiveTrueOrderByBankNameAscAccountNumberAsc();

    boolean existsByAccountNumberIgnoreCase(String accountNumber);

    boolean existsByLinkedAccountId(Long linkedAccountId);
}
