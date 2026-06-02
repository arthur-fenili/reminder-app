package com.ytreminder.repository;

import com.ytreminder.model.BillingRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillingRecordRepository extends JpaRepository<BillingRecord, Long> {
    Page<BillingRecord> findByMemberId(Long memberId, Pageable pageable);
}
