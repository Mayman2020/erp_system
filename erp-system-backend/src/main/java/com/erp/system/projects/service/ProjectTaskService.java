package com.erp.system.projects.service;

        import com.erp.system.common.exception.ResourceNotFoundException;
        import com.erp.system.erp.service.ActivityLogService;
        import com.erp.system.projects.domain.ProjectTask;
        import com.erp.system.projects.dto.display.ProjectTaskDisplayDto;
        import com.erp.system.projects.dto.form.ProjectTaskFormDto;
        import com.erp.system.projects.repository.ProjectTaskRepository;
        import lombok.RequiredArgsConstructor;
        import org.springframework.stereotype.Service;
        import org.springframework.transaction.annotation.Transactional;

        import java.util.List;
        import com.erp.system.projects.domain.TaskPriority;
import com.erp.system.projects.domain.TaskStatus;

        @Service
        @RequiredArgsConstructor
        public class ProjectTaskService {

            private static final String MODULE = "PROJECTS";

            private final ProjectTaskRepository projectTaskRepository;
            private final ActivityLogService activityLogService;

            @Transactional(readOnly = true)
            public List<ProjectTaskDisplayDto> getAll() {
                return projectTaskRepository.findAllByOrderByIdDesc().stream()
                        .map(this::toDisplay)
                        .toList();
            }

            @Transactional(readOnly = true)
            public ProjectTaskDisplayDto getById(Long id) {
                return toDisplay(loadProjectTask(id));
            }

            @Transactional
            public ProjectTaskDisplayDto create(ProjectTaskFormDto request) {
                ProjectTask projectTask = new ProjectTask();
                applyForm(projectTask, request);
                projectTask = projectTaskRepository.save(projectTask);
                activityLogService.log(MODULE, "CREATE", "ProjectTask", projectTask.getId(), String.valueOf(projectTask.getId()),
                        "Created ProjectTask " + projectTask.getId());
                return toDisplay(projectTask);
            }

            @Transactional
            public ProjectTaskDisplayDto update(Long id, ProjectTaskFormDto request) {
                ProjectTask projectTask = loadProjectTask(id);
                applyForm(projectTask, request);
                projectTask = projectTaskRepository.save(projectTask);
                activityLogService.log(MODULE, "UPDATE", "ProjectTask", projectTask.getId(), String.valueOf(projectTask.getId()),
                        "Updated ProjectTask " + projectTask.getId());
                return toDisplay(projectTask);
            }

            @Transactional
            public void delete(Long id) {
                ProjectTask projectTask = loadProjectTask(id);
                projectTaskRepository.delete(projectTask);
                activityLogService.log(MODULE, "DELETE", "ProjectTask", id, String.valueOf(id),
                        "Deleted ProjectTask " + id);
            }

            private ProjectTask loadProjectTask(Long id) {
                return projectTaskRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("ProjectTask", id));
            }

            private void applyForm(ProjectTask projectTask, ProjectTaskFormDto request) {

                projectTask.setProjectId(request.getProjectId());
                projectTask.setTitle(request.getTitle().trim());
                projectTask.setDescription(request.getDescription());
                projectTask.setAssignedEmployeeId(request.getAssignedEmployeeId());
                projectTask.setDueDate(request.getDueDate());
                projectTask.setStatus(request.getStatus());
                projectTask.setPriority(request.getPriority());

            }

            private ProjectTaskDisplayDto toDisplay(ProjectTask projectTask) {
                return ProjectTaskDisplayDto.builder()
                        .id(projectTask.getId())

                        .projectId(projectTask.getProjectId())
                        .title(projectTask.getTitle())
                        .description(projectTask.getDescription())
                        .assignedEmployeeId(projectTask.getAssignedEmployeeId())
                        .dueDate(projectTask.getDueDate())
                        .status(projectTask.getStatus())
                        .priority(projectTask.getPriority())

                        .createdAt(projectTask.getCreatedAt())
                        .updatedAt(projectTask.getUpdatedAt())
                        .build();
            }

        }
