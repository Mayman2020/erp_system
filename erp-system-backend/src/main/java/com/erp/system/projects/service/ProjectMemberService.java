package com.erp.system.projects.service;

import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.projects.domain.ProjectMember;
import com.erp.system.projects.dto.display.ProjectMemberDisplayDto;
import com.erp.system.projects.dto.form.ProjectMemberFormDto;
import com.erp.system.projects.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ProjectMemberService {

    private static final String MODULE = "PROJECTS";

    private final ProjectMemberRepository projectMemberRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<ProjectMemberDisplayDto> getAll() {
        return projectMemberRepository.findAllByOrderByIdDesc().stream()
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectMemberDisplayDto getById(Long id) {
        return toDisplay(loadProjectMember(id));
    }

    @Transactional
    public ProjectMemberDisplayDto create(ProjectMemberFormDto request) {
        ProjectMember projectMember = new ProjectMember();
        applyForm(projectMember, request);
        projectMember = projectMemberRepository.save(projectMember);
        activityLogService.log(MODULE, "CREATE", "ProjectMember", projectMember.getId(), String.valueOf(projectMember.getId()),
                "Created ProjectMember " + projectMember.getId());
        return toDisplay(projectMember);
    }

    @Transactional
    public ProjectMemberDisplayDto update(Long id, ProjectMemberFormDto request) {
        ProjectMember projectMember = loadProjectMember(id);
        applyForm(projectMember, request);
        projectMember = projectMemberRepository.save(projectMember);
        activityLogService.log(MODULE, "UPDATE", "ProjectMember", projectMember.getId(), String.valueOf(projectMember.getId()),
                "Updated ProjectMember " + projectMember.getId());
        return toDisplay(projectMember);
    }

    @Transactional
    public void delete(Long id) {
        ProjectMember projectMember = loadProjectMember(id);
        projectMemberRepository.delete(projectMember);
        activityLogService.log(MODULE, "DELETE", "ProjectMember", id, String.valueOf(id),
                "Deleted ProjectMember " + id);
    }

    private ProjectMember loadProjectMember(Long id) {
        return projectMemberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectMember", id));
    }

    private void applyForm(ProjectMember projectMember, ProjectMemberFormDto request) {

        projectMember.setProjectId(request.getProjectId());
        projectMember.setEmployeeId(request.getEmployeeId());
        projectMember.setRole(request.getRole());

    }

    private ProjectMemberDisplayDto toDisplay(ProjectMember projectMember) {
        return ProjectMemberDisplayDto.builder()
                .id(projectMember.getId())

                .projectId(projectMember.getProjectId())
                .employeeId(projectMember.getEmployeeId())
                .role(projectMember.getRole())

                .createdAt(projectMember.getCreatedAt())
                .updatedAt(projectMember.getUpdatedAt())
                .build();
    }

}
