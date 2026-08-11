package com.erp.system.hr.repository;

import com.erp.system.hr.domain.AttendanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findAllByOrderByIdDesc();

    List<AttendanceRecord> findByAttendanceDateBetween(LocalDate from, LocalDate to);

    long countByEmployeeIdAndAttendanceDateBetween(Long employeeId, LocalDate from, LocalDate to);

    @Query("""
            SELECT a FROM AttendanceRecord a
            WHERE (:employeeId IS NULL OR a.employeeId = :employeeId)
              AND (:status IS NULL OR a.status = :status)
              AND (:fromDate IS NULL OR a.attendanceDate >= :fromDate)
              AND (:toDate IS NULL OR a.attendanceDate <= :toDate)
            """)
    Page<AttendanceRecord> findPaged(@Param("employeeId") Long employeeId,
                                     @Param("status") String status,
                                     @Param("fromDate") LocalDate fromDate,
                                     @Param("toDate") LocalDate toDate,
                                     Pageable pageable);

    @Query("""
            SELECT a FROM AttendanceRecord a
            WHERE (:employeeId IS NULL OR a.employeeId = :employeeId)
              AND (:status IS NULL OR a.status = :status)
              AND (:fromDate IS NULL OR a.attendanceDate >= :fromDate)
              AND (:toDate IS NULL OR a.attendanceDate <= :toDate)
              AND (LOWER(a.status) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(a.notes, '')) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<AttendanceRecord> searchPaged(@Param("employeeId") Long employeeId,
                                       @Param("status") String status,
                                       @Param("fromDate") LocalDate fromDate,
                                       @Param("toDate") LocalDate toDate,
                                       @Param("q") String q,
                                       Pageable pageable);
}
