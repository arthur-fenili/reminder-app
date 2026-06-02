package com.ytreminder.controller;

import com.ytreminder.model.BillingRecord;
import com.ytreminder.model.BillingStatus;
import com.ytreminder.repository.BillingRecordRepository;
import com.ytreminder.service.ReminderJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final BillingRecordRepository billingRecordRepository;
    private final ReminderJob reminderJob;

    public BillingController(BillingRecordRepository billingRecordRepository, ReminderJob reminderJob) {
        this.billingRecordRepository = billingRecordRepository;
        this.reminderJob = reminderJob;
    }

    @GetMapping("/history")
    public ResponseEntity<Object> getHistory(
            @RequestParam(required = false) Long memberId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        var pageable = PageRequest.of(page - 1, pageSize, Sort.by("sentAt").descending());

        Page<BillingRecord> result = memberId != null
                ? billingRecordRepository.findByMemberId(memberId, pageable)
                : billingRecordRepository.findAll(pageable);

        var records = result.getContent().stream().map(BillingRecordDto::from).toList();

        return ResponseEntity.ok(Map.of(
                "total", result.getTotalElements(),
                "page", page,
                "pageSize", pageSize,
                "records", records
        ));
    }

    @PostMapping("/trigger")
    public ResponseEntity<Map<String, String>> triggerManual() {
        reminderJob.execute();
        return ResponseEntity.ok(Map.of("message", "Job executado manualmente com sucesso."));
    }

    record MemberSummary(Long id, String name, String email) {}

    record BillingRecordDto(Long id, LocalDateTime sentAt, BillingStatus status, String errorMessage, MemberSummary member) {
        static BillingRecordDto from(BillingRecord r) {
            return new BillingRecordDto(
                    r.getId(),
                    r.getSentAt(),
                    r.getStatus(),
                    r.getErrorMessage(),
                    new MemberSummary(r.getMember().getId(), r.getMember().getName(), r.getMember().getEmail())
            );
        }
    }
}
