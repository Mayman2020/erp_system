package com.erp.system.projects.repository;

import com.erp.system.projects.domain.ProjectTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectTaskRepository extends JpaRepository<ProjectTask, Long> {
    List<ProjectTask> findAllByOrderByIdDesc();

}
