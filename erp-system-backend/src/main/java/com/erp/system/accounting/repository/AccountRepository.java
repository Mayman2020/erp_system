package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.dto.display.AccountTypeAmountDto;
import com.erp.system.common.enums.AccountingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
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

    @Query("""
            select new com.erp.system.accounting.dto.display.AccountTypeAmountDto(
                a.accountType,
                coalesce(sum(
                    case
                        when a.openingBalanceSide = com.erp.system.accounting.domain.Account$BalanceSide.CREDIT
                            then -coalesce(a.openingBalance, 0)
                        else coalesce(a.openingBalance, 0)
                    end
                ), 0)
            )
            from Account a
            where a.active = true
            group by a.accountType
            """)
    List<AccountTypeAmountDto> aggregateSignedOpeningBalancesByType();

    Optional<Account> findByCode(String code);

    /**
     * Ledger rollup: selected account plus every active descendant (by {@link Account#getFullPath()} prefix).
     */
    @Query("""
            SELECT a.id FROM Account a
            WHERE a.id = :rootId
               OR (a.active = true AND a.fullPath IS NOT NULL AND a.fullPath LIKE CONCAT(:rootPath, '/%'))
            ORDER BY a.code
            """)
    List<Long> findLedgerSubtreeAccountIds(@Param("rootId") Long rootId, @Param("rootPath") String rootPath);

    /**
     * Detail (leaf) accounts only — same rows where users normally set opening balance in COA.
     * Used by the ledger so parent rollups do not double-count a parent opening plus child openings.
     */
    @Query("""
            SELECT a FROM Account a
            WHERE a.id IN :ids
              AND NOT EXISTS (SELECT 1 FROM Account c WHERE c.parent.id = a.id)
            """)
    List<Account> findLedgerLeafAccountsAmongIds(@Param("ids") Collection<Long> ids);
}
