package com.erp.system.projects.service;

import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.projects.domain.ProjectExpense;
import com.erp.system.projects.dto.display.ProjectExpenseDisplayDto;
import com.erp.system.projects.dto.form.ProjectExpenseFormDto;
import com.erp.system.projects.repository.ProjectExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProjectExpenseService {

    private static final String MODULE = "PROJECTS";

    private final ProjectExpenseRepository projectExpenseRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<ProjectExpenseDisplayDto> getAll() {
        return projectExpenseRepository.findAllByOrderByIdDesc().stream()
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectExpenseDisplayDto getById(Long id) {
        return toDisplay(loadProjectExpense(id));
    }

    @Transactional
    public ProjectExpenseDisplayDto create(ProjectExpenseFormDto request) {
        ProjectExpense projectExpense = new ProjectExpense();
        applyForm(projectExpense, request);
        projectExpense = projectExpenseRepository.save(projectExpense);
        activityLogService.log(MODULE, "CREATE", "ProjectExpense", projectExpense.getId(), String.valueOf(projectExpense.getId()),
                "Created ProjectExpense " + projectExpense.getId());
        return toDisplay(projectExpense);
    }

    @Transactional
    public ProjectExpenseDisplayDto update(Long id, ProjectExpenseFormDto request) {
        ProjectExpense projectExpense = loadProjectExpense(id);
        if (projectExpense.getStatus() == TransactionStatus.APPROVED) {
            throw new BusinessException("Approved expense cannot be edited");
        }
        applyForm(projectExpense, request);
        projectExpense = projectExpenseRepository.save(projectExpense);
        activityLogService.log(MODULE, "UPDATE", "ProjectExpense", projectExpense.getId(), String.valueOf(projectExpense.getId()),
                "Updated ProjectExpense " + projectExpense.getId());
        return toDisplay(projectExpense);
    }

    @Transactional
    public ProjectExpenseDisplayDto approve(Long id, String actor) {
        ProjectExpense projectExpense = loadProjectExpense(id);
        if (projectExpense.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Cancelled expense cannot be approved");
        }
        projectExpense.setStatus(TransactionStatus.APPROVED);
        projectExpense = projectExpenseRepository.save(projectExpense);
        activityLogService.log(MODULE, "APPROVE", "ProjectExpense", projectExpense.getId(), String.valueOf(projectExpense.getId()),
                "Approved project expense " + projectExpense.getId());
        return toDisplay(projectExpense);
    }

    @Transactional
    public ProjectExpenseDisplayDto cancel(Long id, String actor, String reason) {
        ProjectExpense projectExpense = loadProjectExpense(id);
        projectExpense.setStatus(TransactionStatus.CANCELLED);
        projectExpense = projectExpenseRepository.save(projectExpense);
        activityLogService.log(MODULE, "CANCEL", "ProjectExpense", projectExpense.getId(), String.valueOf(projectExpense.getId()),
                "Cancelled project expense " + projectExpense.getId());
        return toDisplay(projectExpense);
    }

    @Transactional
    public void delete(Long id) {
        ProjectExpense projectExpense = loadProjectExpense(id);
        if (projectExpense.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft expenses can be deleted");
        }
        projectExpenseRepository.delete(projectExpense);
        activityLogService.log(MODULE, "DELETE", "ProjectExpense", id, String.valueOf(id),
                "Deleted ProjectExpense " + id);
    }

    private ProjectExpense loadProjectExpense(Long id) {
        return projectExpenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectExpense", id));
    }

    private void applyForm(ProjectExpense projectExpense, ProjectExpenseFormDto request) {

        projectExpense.setProjectId(request.getProjectId());
        projectExpense.setExpenseDate(request.getExpenseDate());
        projectExpense.setDescription(request.getDescription().trim());
        projectExpense.setAmount(request.getAmount());

    }

    private ProjectExpenseDisplayDto toDisplay(ProjectExpense projectExpense) {
        return ProjectExpenseDisplayDto.builder()
                .id(projectExpense.getId())

                .projectId(projectExpense.getProjectId())
                .expenseDate(projectExpense.getExpenseDate())
                .description(projectExpense.getDescription())
                .amount(projectExpense.getAmount())
                .status(projectExpense.getStatus())

                .createdAt(projectExpense.getCreatedAt())
                .updatedAt(projectExpense.getUpdatedAt())
                .build();
    }

}
