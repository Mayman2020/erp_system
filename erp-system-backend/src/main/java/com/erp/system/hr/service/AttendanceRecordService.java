package com.erp.system.hr.service;

import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.hr.domain.AttendanceRecord;
import com.erp.system.hr.dto.display.AttendanceRecordDisplayDto;
import com.erp.system.hr.dto.form.AttendanceRecordFormDto;
import com.erp.system.hr.repository.AttendanceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AttendanceRecordService {

    private static final String MODULE = "HR";

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<AttendanceRecordDisplayDto> getAll() {
        return attendanceRecordRepository.findAllByOrderByIdDesc().stream()
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public AttendanceRecordDisplayDto getById(Long id) {
        return toDisplay(loadAttendanceRecord(id));
    }

    @Transactional
    public AttendanceRecordDisplayDto create(AttendanceRecordFormDto request) {
        AttendanceRecord attendanceRecord = new AttendanceRecord();
        applyForm(attendanceRecord, request);
        attendanceRecord = attendanceRecordRepository.save(attendanceRecord);
        activityLogService.log(MODULE, "CREATE", "AttendanceRecord", attendanceRecord.getId(), String.valueOf(attendanceRecord.getId()),
                "Created AttendanceRecord " + attendanceRecord.getId());
        return toDisplay(attendanceRecord);
    }

    @Transactional
    public AttendanceRecordDisplayDto update(Long id, AttendanceRecordFormDto request) {
        AttendanceRecord attendanceRecord = loadAttendanceRecord(id);
        applyForm(attendanceRecord, request);
        attendanceRecord = attendanceRecordRepository.save(attendanceRecord);
        activityLogService.log(MODULE, "UPDATE", "AttendanceRecord", attendanceRecord.getId(), String.valueOf(attendanceRecord.getId()),
                "Updated AttendanceRecord " + attendanceRecord.getId());
        return toDisplay(attendanceRecord);
    }

    @Transactional
    public void delete(Long id) {
        AttendanceRecord attendanceRecord = loadAttendanceRecord(id);
        attendanceRecordRepository.delete(attendanceRecord);
        activityLogService.log(MODULE, "DELETE", "AttendanceRecord", id, String.valueOf(id),
                "Deleted AttendanceRecord " + id);
    }

    private AttendanceRecord loadAttendanceRecord(Long id) {
        return attendanceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceRecord", id));
    }

    private void applyForm(AttendanceRecord attendanceRecord, AttendanceRecordFormDto request) {

        attendanceRecord.setEmployeeId(request.getEmployeeId());
        attendanceRecord.setAttendanceDate(request.getAttendanceDate());
        attendanceRecord.setCheckIn(request.getCheckIn());
        attendanceRecord.setCheckOut(request.getCheckOut());
        attendanceRecord.setStatus(request.getStatus().trim());
        attendanceRecord.setNotes(request.getNotes());

    }

    private AttendanceRecordDisplayDto toDisplay(AttendanceRecord attendanceRecord) {
        return AttendanceRecordDisplayDto.builder()
                .id(attendanceRecord.getId())

                .employeeId(attendanceRecord.getEmployeeId())
                .attendanceDate(attendanceRecord.getAttendanceDate())
                .checkIn(attendanceRecord.getCheckIn())
                .checkOut(attendanceRecord.getCheckOut())
                .status(attendanceRecord.getStatus())
                .notes(attendanceRecord.getNotes())

                .createdAt(attendanceRecord.getCreatedAt())
                .updatedAt(attendanceRecord.getUpdatedAt())
                .build();
    }

}
