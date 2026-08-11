package com.erp.system.pmo.service;

import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.pmo.domain.PmoRisk;
import com.erp.system.pmo.dto.display.PmoRiskDisplayDto;
import com.erp.system.pmo.dto.form.PmoRiskFormDto;
import com.erp.system.pmo.repository.PmoRiskRepository;
import com.erp.system.projects.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PmoRiskService {

    private static final String MODULE = "PMO";

    private final PmoRiskRepository pmoRiskRepository;
    private final ProjectRepository projectRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<PmoRiskDisplayDto> getByProject(Long projectId) {
        ensureProject(projectId);
        return pmoRiskRepository.findByProjectIdOrderByIdDesc(projectId).stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public PmoRiskDisplayDto getById(Long projectId, Long id) {
        return toDisplay(load(projectId, id));
    }

    @Transactional
    public PmoRiskDisplayDto create(Long projectId, PmoRiskFormDto request) {
        ensureProject(projectId);
        PmoRisk entity = new PmoRisk();
        entity.setProjectId(projectId);
        applyForm(entity, request);
        entity = pmoRiskRepository.save(entity);
        activityLogService.log(MODULE, "CREATE", "PmoRisk", entity.getId(), entity.getTitle(), "Created risk");
        return toDisplay(entity);
    }

    @Transactional
    public PmoRiskDisplayDto update(Long projectId, Long id, PmoRiskFormDto request) {
        PmoRisk entity = load(projectId, id);
        applyForm(entity, request);
        entity = pmoRiskRepository.save(entity);
        activityLogService.log(MODULE, "UPDATE", "PmoRisk", entity.getId(), entity.getTitle(), "Updated risk");
        return toDisplay(entity);
    }

    @Transactional
    public void delete(Long projectId, Long id) {
        PmoRisk entity = load(projectId, id);
        pmoRiskRepository.delete(entity);
        activityLogService.log(MODULE, "DELETE", "PmoRisk", id, entity.getTitle(), "Deleted risk");
    }

    private void ensureProject(Long projectId) {
        projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
    }

    private PmoRisk load(Long projectId, Long id) {
        PmoRisk entity = pmoRiskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("PmoRisk", id));
        if (!projectId.equals(entity.getProjectId())) {
            throw new ResourceNotFoundException("PmoRisk", id);
        }
        return entity;
    }

    private void applyForm(PmoRisk entity, PmoRiskFormDto request) {
        entity.setTitle(request.getTitle());
        entity.setSeverity(request.getSeverity());
        entity.setStatus(request.getStatus());
        entity.setMitigation(request.getMitigation());
    }

    private PmoRiskDisplayDto toDisplay(PmoRisk entity) {
        return PmoRiskDisplayDto.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .title(entity.getTitle())
                .severity(entity.getSeverity())
                .status(entity.getStatus())
                .mitigation(entity.getMitigation())
                .build();
    }
}
