package com.erp.system.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "numbering_sequences", schema = "erp_system")
@Getter
@Setter
public class NumberingSequence extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sequence_name", nullable = false, unique = true)
    private String sequenceName;

    @Column(name = "prefix")
    private String prefix;

    @Column(name = "current_number", nullable = false)
    private Long currentNumber = 0L;

    @Column(name = "padding_length", nullable = false)
    private Integer paddingLength = 4;

    public String generateNextNumber() {
        this.currentNumber++;
        String numberStr = String.format("%0" + paddingLength + "d", currentNumber);
        return (prefix != null ? prefix : "") + numberStr;
    }
}