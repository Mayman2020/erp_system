package com.erp.system.accounting.controller;

import com.erp.system.accounting.dto.display.JournalEntryDisplayDto;
import com.erp.system.accounting.dto.form.JournalEntryFormDto;
import com.erp.system.accounting.service.JournalEntryService;
import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.enums.JournalEntryStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/accounting/journal-entries")
@RequiredArgsConstructor
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    @GetMapping
    public ApiResponse<List<JournalEntryDisplayDto>> getJournalEntries(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) JournalEntryStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        if (page > 0 || size != 50) {
            Pageable pageable = PageRequest.of(page, size);
            Page<JournalEntryDisplayDto> result = journalEntryService.getJournalEntries(pageable);
            return ApiResponse.success(result.getContent());
        } else {
            List<JournalEntryDisplayDto> result = journalEntryService.searchJournalEntries(search, status, fromDate, toDate, accountId);
            return ApiResponse.success(result);
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<JournalEntryDisplayDto> getJournalEntry(@PathVariable Long id) {
        return ApiResponse.success(journalEntryService.getJournalEntry(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<JournalEntryDisplayDto> createJournalEntry(@Valid @RequestBody JournalEntryFormDto request) {
        return ApiResponse.success(journalEntryService.createJournalEntry(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<JournalEntryDisplayDto> updateJournalEntry(
            @PathVariable Long id,
            @Valid @RequestBody JournalEntryFormDto request
    ) {
        return ApiResponse.success(journalEntryService.updateJournalEntry(id, request));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<JournalEntryDisplayDto> approveJournalEntry(
            @PathVariable Long id,
            @RequestParam String approvedBy
    ) {
        return ApiResponse.success(journalEntryService.approveJournalEntry(id, approvedBy));
    }

    @PostMapping("/{id}/post")
    public ApiResponse<JournalEntryDisplayDto> postJournalEntry(
            @PathVariable Long id,
            @RequestParam String postedBy
    ) {
        return ApiResponse.success(journalEntryService.postJournalEntry(id, postedBy));
    }

    @PostMapping("/{id}/reverse")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<JournalEntryDisplayDto> reverseJournalEntry(
            @PathVariable Long id,
            @RequestParam String reversedBy,
            @RequestParam(required = false) String reason
    ) {
        return ApiResponse.success(journalEntryService.reverseJournalEntry(id, reversedBy, reason));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteJournalEntry(@PathVariable Long id) {
        journalEntryService.deleteJournalEntry(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<JournalEntryDisplayDto> cancelJournalEntry(@PathVariable Long id) {
        return ApiResponse.success(journalEntryService.cancelJournalEntry(id));
    }
}
