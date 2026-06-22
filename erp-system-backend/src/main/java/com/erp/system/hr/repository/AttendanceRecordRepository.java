package com.erp.system.hr.repository;

import com.erp.system.hr.domain.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findAllByOrderByIdDesc();

}
