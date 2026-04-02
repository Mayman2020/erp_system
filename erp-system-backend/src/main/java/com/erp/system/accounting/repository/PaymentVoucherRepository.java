package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.PaymentVoucher;
import com.erp.system.common.enums.VoucherStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentVoucherRepository extends JpaRepository<PaymentVoucher, Long> {

    @EntityGraph(attributePaths = {"cashAccount", "expenseAccount", "journalEntry"})
    List<PaymentVoucher> findAllByOrderByVoucherDateDescIdDesc();

    @EntityGraph(attributePaths = {"cashAccount", "expenseAccount", "journalEntry"})
    Optional<PaymentVoucher> findById(Long id);

    boolean existsByReferenceIgnoreCase(String reference);

    long countByVoucherDateBetween(LocalDate from, LocalDate to);

    List<PaymentVoucher> findByStatusOrderByVoucherDateDescIdDesc(VoucherStatus status);

    List<PaymentVoucher> findByBillIdOrderByVoucherDateAscIdAsc(Long billId);
}
