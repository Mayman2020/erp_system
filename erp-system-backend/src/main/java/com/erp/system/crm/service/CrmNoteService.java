package com.erp.system.crm.service;

import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.crm.domain.CrmNote;
import com.erp.system.crm.dto.display.CrmNoteDisplayDto;
import com.erp.system.crm.dto.form.CrmNoteFormDto;
import com.erp.system.crm.repository.CrmNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class CrmNoteService {

    private static final String MODULE = "CRM";

    private final CrmNoteRepository crmNoteRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<CrmNoteDisplayDto> getAll() {
        return crmNoteRepository.findAllByOrderByIdDesc().stream()
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public CrmNoteDisplayDto getById(Long id) {
        return toDisplay(loadCrmNote(id));
    }

    @Transactional
    public CrmNoteDisplayDto create(CrmNoteFormDto request) {
        CrmNote crmNote = new CrmNote();
        applyForm(crmNote, request);
        crmNote = crmNoteRepository.save(crmNote);
        activityLogService.log(MODULE, "CREATE", "CrmNote", crmNote.getId(), String.valueOf(crmNote.getId()),
                "Created CrmNote " + crmNote.getId());
        return toDisplay(crmNote);
    }

    @Transactional
    public CrmNoteDisplayDto update(Long id, CrmNoteFormDto request) {
        CrmNote crmNote = loadCrmNote(id);
        applyForm(crmNote, request);
        crmNote = crmNoteRepository.save(crmNote);
        activityLogService.log(MODULE, "UPDATE", "CrmNote", crmNote.getId(), String.valueOf(crmNote.getId()),
                "Updated CrmNote " + crmNote.getId());
        return toDisplay(crmNote);
    }

    @Transactional
    public void delete(Long id) {
        CrmNote crmNote = loadCrmNote(id);
        crmNoteRepository.delete(crmNote);
        activityLogService.log(MODULE, "DELETE", "CrmNote", id, String.valueOf(id),
                "Deleted CrmNote " + id);
    }

    private CrmNote loadCrmNote(Long id) {
        return crmNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CrmNote", id));
    }

    private void applyForm(CrmNote crmNote, CrmNoteFormDto request) {

        crmNote.setCustomerId(request.getCustomerId());
        crmNote.setLeadId(request.getLeadId());
        crmNote.setNoteText(request.getNoteText().trim());

    }

    private CrmNoteDisplayDto toDisplay(CrmNote crmNote) {
        return CrmNoteDisplayDto.builder()
                .id(crmNote.getId())

                .customerId(crmNote.getCustomerId())
                .leadId(crmNote.getLeadId())
                .noteText(crmNote.getNoteText())

                .createdAt(crmNote.getCreatedAt())
                .updatedAt(crmNote.getUpdatedAt())
                .build();
    }

}
