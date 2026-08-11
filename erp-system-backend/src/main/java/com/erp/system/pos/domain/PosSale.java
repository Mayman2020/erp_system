package com.erp.system.pos.domain;

import com.erp.system.common.entity.BaseEntity;
import com.erp.system.inventory.domain.Warehouse;
import com.erp.system.sales.domain.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pos_sales", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosSale extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sale_no", nullable = false, length = 40, unique = true)
    private String saleNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shift_id", nullable = false)
    private PosShift shift;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "COMPLETED";

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "payment_method", nullable = false, length = 20)
    @Builder.Default
    private String paymentMethod = "CASH";

    @Column(name = "paid_cash", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal paidCash = BigDecimal.ZERO;

    @Column(name = "paid_card", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal paidCard = BigDecimal.ZERO;

    @Column(name = "paid_credit", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal paidCredit = BigDecimal.ZERO;

    @Column(name = "idempotency_key", length = 80, unique = true)
    private String idempotencyKey;

    @Column(name = "offline_batch_id", length = 80)
    private String offlineBatchId;

    @Column(name = "sales_invoice_id")
    private Long salesInvoiceId;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PosSaleLine> lines = new ArrayList<>();
}
