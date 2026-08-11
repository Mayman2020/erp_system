package com.erp.system.pmo.service;

import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.pmo.domain.PmoIssue;
import com.erp.system.pmo.dto.display.PmoIssueDisplayDto;
import com.erp.system.pmo.dto.form.PmoIssueFormDto;
import com.erp.system.pmo.repository.PmoIssueRepository;
import com.erp.system.projects.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PmoIssueService {

    private static final String MODULE = "PMO";

    private final PmoIssueRepository pmoIssueRepository;
    private final ProjectRepository projectRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<PmoIssueDisplayDto> getByProject(Long projectId) {
        ensureProject(projectId);
        return pmoIssueRepository.findByProjectIdOrderByIdDesc(projectId).stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public PmoIssueDisplayDto getById(Long projectId, Long id) {
        return toDisplay(load(projectId, id));
    }

    @Transactional
    public PmoIssueDisplayDto create(Long projectId, PmoIssueFormDto request) {
        ensureProject(projectId);
        PmoIssue entity = new PmoIssue();
        entity.setProjectId(projectId);
        applyForm(entity, request);
        entity = pmoIssueRepository.save(entity);
        activityLogService.log(MODULE, "CREATE", "PmoIssue", entity.getId(), entity.getTitle(), "Created issue");
        return toDisplay(entity);
    }

    @Transactional
    public PmoIssueDisplayDto update(Long projectId, Long id, PmoIssueFormDto request) {
        PmoIssue entity = load(projectId, id);
        applyForm(entity, request);
        entity = pmoIssueRepository.save(entity);
        activityLogService.log(MODULE, "UPDATE", "PmoIssue", entity.getId(), entity.getTitle(), "Updated issue");
        return toDisplay(entity);
    }

    @Transactional
    public void delete(Long projectId, Long id) {
        PmoIssue entity = load(projectId, id);
        pmoIssueRepository.delete(entity);
        activityLogService.log(MODULE, "DELETE", "PmoIssue", id, entity.getTitle(), "Deleted issue");
    }

    private void ensureProject(Long projectId) {
        projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
    }

    private PmoIssue load(Long projectId, Long id) {
        PmoIssue entity = pmoIssueRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("PmoIssue", id));
        if (!projectId.equals(entity.getProjectId())) {
            throw new ResourceNotFoundException("PmoIssue", id);
        }
        return entity;
    }

    private void applyForm(PmoIssue entity, PmoIssueFormDto request) {
        entity.setTitle(request.getTitle());
        entity.setStatus(request.getStatus());
        entity.setOwnerName(request.getOwnerName());
        entity.setNotes(request.getNotes());
    }

    private PmoIssueDisplayDto toDisplay(PmoIssue entity) {
        return PmoIssueDisplayDto.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .title(entity.getTitle())
                .status(entity.getStatus())
                .ownerName(entity.getOwnerName())
                .notes(entity.getNotes())
                .build();
    }
}
