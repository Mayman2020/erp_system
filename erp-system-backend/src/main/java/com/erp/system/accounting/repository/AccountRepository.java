package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.Account;
import com.erp.system.common.enums.AccountingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findAllByOrderByCodeAsc();

    List<Account> findByActiveTrueOrderByCodeAsc();

    List<Account> findByParentIsNullOrderByCodeAsc();

    List<Account> findByParentIdOrderByCodeAsc(Long parentId);

    List<Account> findByAccountTypeAndActiveTrue(AccountingType accountType);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    long countByActiveTrue();

    @Query("SELECT a FROM Account a WHERE a.parent IS NULL ORDER BY a.code")
    List<Account> findRootAccounts();

    @Query("SELECT a FROM Account a WHERE LOWER(a.code) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(a.nameEn) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(a.nameAr) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Account> searchAccounts(@Param("search") String search);

    @Query("SELECT a FROM Account a WHERE a.accountType = :type AND a.active = true ORDER BY a.code")
    List<Account> findByTypeAndActive(@Param("type") AccountingType type);

    Optional<Account> findByCode(String code);
}
