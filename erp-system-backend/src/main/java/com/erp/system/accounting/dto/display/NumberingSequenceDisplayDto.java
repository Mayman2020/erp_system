package com.erp.system.accounting.dto.display;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NumberingSequenceDisplayDto {

    private Long id;
    private String sequenceName;
    private String prefix;
    private Long currentNumber;
    private Integer paddingLength;
}
