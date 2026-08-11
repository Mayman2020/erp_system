package com.erp.system.organization.domain;

import lombok.*;
import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class UserCompanyAccessId implements Serializable {
    private Long user;
    private Long company;
}
