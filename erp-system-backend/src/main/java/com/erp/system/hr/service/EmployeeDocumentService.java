package com.erp.system.hr.service;

import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.hr.domain.EmployeeDocument;
import com.erp.system.hr.dto.display.EmployeeDocumentDisplayDto;
import com.erp.system.hr.dto.form.EmployeeDocumentFormDto;
import com.erp.system.hr.repository.EmployeeDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class EmployeeDocumentService {

    private static final String MODULE = "HR";

    private final EmployeeDocumentRepository employeeDocumentRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<EmployeeDocumentDisplayDto> getAll() {
        return employeeDocumentRepository.findAllByOrderByIdDesc().stream()
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeDocumentDisplayDto getById(Long id) {
        return toDisplay(loadEmployeeDocument(id));
    }

    @Transactional
    public EmployeeDocumentDisplayDto create(EmployeeDocumentFormDto request) {
        EmployeeDocument employeeDocument = new EmployeeDocument();
        applyForm(employeeDocument, request);
        employeeDocument = employeeDocumentRepository.save(employeeDocument);
        activityLogService.log(MODULE, "CREATE", "EmployeeDocument", employeeDocument.getId(), String.valueOf(employeeDocument.getId()),
                "Created EmployeeDocument " + employeeDocument.getId());
        return toDisplay(employeeDocument);
    }

    @Transactional
    public EmployeeDocumentDisplayDto update(Long id, EmployeeDocumentFormDto request) {
        EmployeeDocument employeeDocument = loadEmployeeDocument(id);
        applyForm(employeeDocument, request);
        employeeDocument = employeeDocumentRepository.save(employeeDocument);
        activityLogService.log(MODULE, "UPDATE", "EmployeeDocument", employeeDocument.getId(), String.valueOf(employeeDocument.getId()),
                "Updated EmployeeDocument " + employeeDocument.getId());
        return toDisplay(employeeDocument);
    }

    @Transactional
    public void delete(Long id) {
        EmployeeDocument employeeDocument = loadEmployeeDocument(id);
        employeeDocumentRepository.delete(employeeDocument);
        activityLogService.log(MODULE, "DELETE", "EmployeeDocument", id, String.valueOf(id),
                "Deleted EmployeeDocument " + id);
    }

    private EmployeeDocument loadEmployeeDocument(Long id) {
        return employeeDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeDocument", id));
    }

    private void applyForm(EmployeeDocument employeeDocument, EmployeeDocumentFormDto request) {

        employeeDocument.setEmployeeId(request.getEmployeeId());
        employeeDocument.setDocumentType(request.getDocumentType().trim());
        employeeDocument.setFileName(request.getFileName().trim());
        employeeDocument.setFilePath(request.getFilePath());
        employeeDocument.setExpiryDate(request.getExpiryDate());

    }

    private EmployeeDocumentDisplayDto toDisplay(EmployeeDocument employeeDocument) {
        return EmployeeDocumentDisplayDto.builder()
                .id(employeeDocument.getId())

                .employeeId(employeeDocument.getEmployeeId())
                .documentType(employeeDocument.getDocumentType())
                .fileName(employeeDocument.getFileName())
                .filePath(employeeDocument.getFilePath())
                .expiryDate(employeeDocument.getExpiryDate())

                .createdAt(employeeDocument.getCreatedAt())
                .updatedAt(employeeDocument.getUpdatedAt())
                .build();
    }

}
