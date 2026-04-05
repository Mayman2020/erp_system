package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.ReceiptVoucher;
import com.erp.system.accounting.dto.display.AccountingDashboardDisplayDto;
import com.erp.system.common.enums.VoucherStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReceiptVoucherRepository extends JpaRepository<ReceiptVoucher, Long> {

    @EntityGraph(attributePaths = {"cashAccount", "revenueAccount", "journalEntry"})
    List<ReceiptVoucher> findAllByOrderByVoucherDateDescIdDesc();

    @EntityGraph(attributePaths = {"cashAccount", "revenueAccount", "journalEntry"})
    Optional<ReceiptVoucher> findById(Long id);

    boolean existsByReferenceIgnoreCase(String reference);

    long countByVoucherDateBetween(LocalDate from, LocalDate to);

    List<ReceiptVoucher> findByStatusOrderByVoucherDateDescIdDesc(VoucherStatus status);

    List<ReceiptVoucher> findByInvoiceReferenceOrderByVoucherDateDescIdDesc(String invoiceReference);

    @Query("""
            select rv.revenueAccount.id, count(rv)
            from ReceiptVoucher rv
            join rv.revenueAccount acc
            where acc.accountType = com.erp.system.common.enums.AccountingType.ASSET
              and rv.status = com.erp.system.common.enums.VoucherStatus.APPROVED
            group by rv.revenueAccount.id
            """)
    List<Object[]> countReceiptsGroupedByAssetRevenueAccountId();

    @Query("""
            select new com.erp.system.accounting.dto.display.AccountingDashboardDisplayDto$RecentDocument(
                v.id,
                v.reference,
                v.voucherDate,
                v.amount,
                cast(v.status as string))
            from ReceiptVoucher v
            where v.status = com.erp.system.common.enums.VoucherStatus.APPROVED
            """)
    Page<AccountingDashboardDisplayDto.RecentDocument> pageDashboardRecentReceipts(Pageable pageable);
}
