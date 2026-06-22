package com.erp.system.crm.repository;

import com.erp.system.crm.domain.CrmNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrmNoteRepository extends JpaRepository<CrmNote, Long> {
    List<CrmNote> findAllByOrderByIdDesc();

}
