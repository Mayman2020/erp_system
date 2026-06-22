package com.erp.system.projects.service;

        import com.erp.system.common.exception.ResourceNotFoundException;
        import com.erp.system.erp.service.ActivityLogService;
        import com.erp.system.projects.domain.Project;
        import com.erp.system.projects.dto.display.ProjectDisplayDto;
        import com.erp.system.projects.dto.form.ProjectFormDto;
        import com.erp.system.projects.repository.ProjectRepository;
        import lombok.RequiredArgsConstructor;
        import org.springframework.stereotype.Service;
        import org.springframework.transaction.annotation.Transactional;

        import java.util.List;
        import java.math.BigDecimal;
import com.erp.system.projects.domain.ProjectStatus;

        @Service
        @RequiredArgsConstructor
        public class ProjectService {

            private static final String MODULE = "PROJECTS";

            private final ProjectRepository projectRepository;
            private final ActivityLogService activityLogService;

            @Transactional(readOnly = true)
            public List<ProjectDisplayDto> getAll() {
                return projectRepository.findAllByOrderByIdDesc().stream()
                        .map(this::toDisplay)
                        .toList();
            }

            @Transactional(readOnly = true)
            public ProjectDisplayDto getById(Long id) {
                return toDisplay(loadProject(id));
            }

            @Transactional
            public ProjectDisplayDto create(ProjectFormDto request) {
                Project project = new Project();
                applyForm(project, request);
                project = projectRepository.save(project);
                activityLogService.log(MODULE, "CREATE", "Project", project.getId(), String.valueOf(project.getId()),
                        "Created Project " + project.getId());
                return toDisplay(project);
            }

            @Transactional
            public ProjectDisplayDto update(Long id, ProjectFormDto request) {
                Project project = loadProject(id);
                applyForm(project, request);
                project = projectRepository.save(project);
                activityLogService.log(MODULE, "UPDATE", "Project", project.getId(), String.valueOf(project.getId()),
                        "Updated Project " + project.getId());
                return toDisplay(project);
            }

            @Transactional
            public void delete(Long id) {
                Project project = loadProject(id);
                projectRepository.delete(project);
                activityLogService.log(MODULE, "DELETE", "Project", id, String.valueOf(id),
                        "Deleted Project " + id);
            }

            private Project loadProject(Long id) {
                return projectRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Project", id));
            }

            private void applyForm(Project project, ProjectFormDto request) {

                project.setProjectCode(request.getProjectCode().trim());
                project.setNameEn(request.getNameEn().trim());
                project.setNameAr(request.getNameAr());
                project.setCustomerId(request.getCustomerId());
                project.setStartDate(request.getStartDate());
                project.setEndDate(request.getEndDate());
                project.setBudget(request.getBudget());
                project.setStatus(request.getStatus());
                project.setDescription(request.getDescription());

            }

            private ProjectDisplayDto toDisplay(Project project) {
                return ProjectDisplayDto.builder()
                        .id(project.getId())

                        .projectCode(project.getProjectCode())
                        .nameEn(project.getNameEn())
                        .nameAr(project.getNameAr())
                        .customerId(project.getCustomerId())
                        .startDate(project.getStartDate())
                        .endDate(project.getEndDate())
                        .budget(project.getBudget())
                        .status(project.getStatus())
                        .description(project.getDescription())

                        .createdAt(project.getCreatedAt())
                        .updatedAt(project.getUpdatedAt())
                        .build();
            }

        }
