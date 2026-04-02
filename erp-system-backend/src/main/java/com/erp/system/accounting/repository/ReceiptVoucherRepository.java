package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.ReceiptVoucher;
import com.erp.system.common.enums.VoucherStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
