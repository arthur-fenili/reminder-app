package com.ytreminder.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "billing_records")
public class BillingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BillingStatus status;

    @Column(name = "error_message")
    private String errorMessage;

    public BillingRecord() {}

    public BillingRecord(Member member, BillingStatus status, String errorMessage) {
        this.member = member;
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public Long getId() { return id; }
    public Member getMember() { return member; }
    public LocalDateTime getSentAt() { return sentAt; }
    public BillingStatus getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
}
