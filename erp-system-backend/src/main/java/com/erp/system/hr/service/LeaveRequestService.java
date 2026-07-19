package com.erp.system.hr.service;

import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.hr.domain.LeaveRequest;
import com.erp.system.hr.dto.display.LeaveRequestDisplayDto;
import com.erp.system.hr.dto.form.LeaveRequestFormDto;
import com.erp.system.hr.repository.LeaveRequestRepository;
import com.erp.system.notification.domain.NotificationType;
import com.erp.system.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private static final String MODULE = "HR";

    private final LeaveRequestRepository leaveRequestRepository;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<LeaveRequestDisplayDto> getAll() {
        return leaveRequestRepository.findAllByOrderByIdDesc().stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public LeaveRequestDisplayDto getById(Long id) {
        return toDisplay(loadLeaveRequest(id));
    }

    @Transactional
    public LeaveRequestDisplayDto create(LeaveRequestFormDto request) {
        LeaveRequest leaveRequest = new LeaveRequest();
        applyForm(leaveRequest, request);
        leaveRequest.setStatus(TransactionStatus.PENDING);
        leaveRequest = leaveRequestRepository.save(leaveRequest);
        activityLogService.log(MODULE, "CREATE", "LeaveRequest", leaveRequest.getId(), String.valueOf(leaveRequest.getId()),
                "Created leave request " + leaveRequest.getId());
        notificationService.notifyAdmins(
                NotificationType.HR,
                "NOTIFICATIONS.LEAVE_SUBMITTED_TITLE",
                "NOTIFICATIONS.LEAVE_SUBMITTED_BODY",
                Map.of("leaveType", leaveRequest.getLeaveType()),
                "LEAVE_REQUEST",
                leaveRequest.getId());
        return toDisplay(leaveRequest);
    }

    @Transactional
    public LeaveRequestDisplayDto update(Long id, LeaveRequestFormDto request) {
        LeaveRequest leaveRequest = loadLeaveRequest(id);
        if (leaveRequest.getStatus() == TransactionStatus.APPROVED) {
            throw new BusinessException("Approved leave request cannot be edited");
        }
        applyForm(leaveRequest, request);
        leaveRequest = leaveRequestRepository.save(leaveRequest);
        activityLogService.log(MODULE, "UPDATE", "LeaveRequest", leaveRequest.getId(), String.valueOf(leaveRequest.getId()),
                "Updated leave request " + leaveRequest.getId());
        return toDisplay(leaveRequest);
    }

    @Transactional
    public LeaveRequestDisplayDto approve(Long id, String actor) {
        LeaveRequest leaveRequest = loadLeaveRequest(id);
        if (leaveRequest.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Cancelled leave request cannot be approved");
        }
        leaveRequest.setStatus(TransactionStatus.APPROVED);
        leaveRequest = leaveRequestRepository.save(leaveRequest);
        activityLogService.log(MODULE, "APPROVE", "LeaveRequest", leaveRequest.getId(), String.valueOf(leaveRequest.getId()),
                "Approved leave request " + leaveRequest.getId());
        notificationService.notifyAdmins(
                NotificationType.HR,
                "NOTIFICATIONS.LEAVE_APPROVED_TITLE",
                "NOTIFICATIONS.LEAVE_APPROVED_BODY",
                Map.of("leaveType", leaveRequest.getLeaveType()),
                "LEAVE_REQUEST",
                leaveRequest.getId());
        return toDisplay(leaveRequest);
    }

    @Transactional
    public LeaveRequestDisplayDto cancel(Long id, String actor, String reason) {
        LeaveRequest leaveRequest = loadLeaveRequest(id);
        leaveRequest.setStatus(TransactionStatus.CANCELLED);
        leaveRequest = leaveRequestRepository.save(leaveRequest);
        activityLogService.log(MODULE, "CANCEL", "LeaveRequest", leaveRequest.getId(), String.valueOf(leaveRequest.getId()),
                "Cancelled leave request " + leaveRequest.getId());
        return toDisplay(leaveRequest);
    }

    @Transactional
    public void delete(Long id) {
        LeaveRequest leaveRequest = loadLeaveRequest(id);
        leaveRequestRepository.delete(leaveRequest);
        activityLogService.log(MODULE, "DELETE", "LeaveRequest", id, String.valueOf(id),
                "Deleted leave request " + id);
    }

    private void applyForm(LeaveRequest leaveRequest, LeaveRequestFormDto request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("End date cannot be before start date");
        }
        leaveRequest.setEmployeeId(request.getEmployeeId());
        leaveRequest.setLeaveType(request.getLeaveType().trim());
        leaveRequest.setStartDate(request.getStartDate());
        leaveRequest.setEndDate(request.getEndDate());
        leaveRequest.setReason(request.getReason());
    }

    private LeaveRequest loadLeaveRequest(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", id));
    }

    private LeaveRequestDisplayDto toDisplay(LeaveRequest leaveRequest) {
        return LeaveRequestDisplayDto.builder()
                .id(leaveRequest.getId())
                .employeeId(leaveRequest.getEmployeeId())
                .leaveType(leaveRequest.getLeaveType())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .status(leaveRequest.getStatus())
                .reason(leaveRequest.getReason())
                .createdAt(leaveRequest.getCreatedAt())
                .updatedAt(leaveRequest.getUpdatedAt())
                .build();
    }
}
