package com.erp.system.pmo.service;

import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.pmo.domain.PmoMilestone;
import com.erp.system.pmo.dto.display.PmoMilestoneDisplayDto;
import com.erp.system.pmo.dto.form.PmoMilestoneFormDto;
import com.erp.system.pmo.repository.PmoMilestoneRepository;
import com.erp.system.projects.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PmoMilestoneService {

    private static final String MODULE = "PMO";

    private final PmoMilestoneRepository pmoMilestoneRepository;
    private final ProjectRepository projectRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<PmoMilestoneDisplayDto> getByProject(Long projectId) {
        ensureProject(projectId);
        return pmoMilestoneRepository.findByProjectIdOrderBySortOrderAscIdAsc(projectId).stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public PmoMilestoneDisplayDto getById(Long projectId, Long id) {
        return toDisplay(load(projectId, id));
    }

    @Transactional
    public PmoMilestoneDisplayDto create(Long projectId, PmoMilestoneFormDto request) {
        ensureProject(projectId);
        PmoMilestone entity = new PmoMilestone();
        entity.setProjectId(projectId);
        applyForm(entity, request);
        entity = pmoMilestoneRepository.save(entity);
        activityLogService.log(MODULE, "CREATE", "PmoMilestone", entity.getId(), entity.getTitle(), "Created milestone");
        return toDisplay(entity);
    }

    @Transactional
    public PmoMilestoneDisplayDto update(Long projectId, Long id, PmoMilestoneFormDto request) {
        PmoMilestone entity = load(projectId, id);
        applyForm(entity, request);
        entity = pmoMilestoneRepository.save(entity);
        activityLogService.log(MODULE, "UPDATE", "PmoMilestone", entity.getId(), entity.getTitle(), "Updated milestone");
        return toDisplay(entity);
    }

    @Transactional
    public void delete(Long projectId, Long id) {
        PmoMilestone entity = load(projectId, id);
        pmoMilestoneRepository.delete(entity);
        activityLogService.log(MODULE, "DELETE", "PmoMilestone", id, entity.getTitle(), "Deleted milestone");
    }

    private void ensureProject(Long projectId) {
        projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
    }

    private PmoMilestone load(Long projectId, Long id) {
        PmoMilestone entity = pmoMilestoneRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("PmoMilestone", id));
        if (!projectId.equals(entity.getProjectId())) {
            throw new ResourceNotFoundException("PmoMilestone", id);
        }
        return entity;
    }

    private void applyForm(PmoMilestone entity, PmoMilestoneFormDto request) {
        entity.setTitle(request.getTitle());
        entity.setDueDate(request.getDueDate());
        entity.setStatus(request.getStatus());
        entity.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
    }

    private PmoMilestoneDisplayDto toDisplay(PmoMilestone entity) {
        return PmoMilestoneDisplayDto.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .title(entity.getTitle())
                .dueDate(entity.getDueDate())
                .status(entity.getStatus())
                .sortOrder(entity.getSortOrder())
                .build();
    }
}
