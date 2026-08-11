package com.erp.system.admin.repository;

import com.erp.system.admin.domain.BackupJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BackupJobRepository extends JpaRepository<BackupJob, Long> {
    List<BackupJob> findAllByOrderByIdDesc();

    Optional<BackupJob> findByJobNoIgnoreCase(String jobNo);
}
