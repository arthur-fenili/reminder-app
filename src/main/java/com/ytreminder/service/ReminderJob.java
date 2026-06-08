package com.ytreminder.service;

import com.ytreminder.model.BillingRecord;
import com.ytreminder.model.BillingStatus;
import com.ytreminder.model.Member;
import com.ytreminder.repository.BillingRecordRepository;
import com.ytreminder.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReminderJob {

    private static final Logger log = LoggerFactory.getLogger(ReminderJob.class);

    private final MemberRepository memberRepository;
    private final BillingRecordRepository billingRecordRepository;
    private final EmailService emailService;

    public ReminderJob(MemberRepository memberRepository,
                       BillingRecordRepository billingRecordRepository,
                       EmailService emailService) {
        this.memberRepository = memberRepository;
        this.billingRecordRepository = billingRecordRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "${reminder.cron}", zone = "America/Sao_Paulo")
    public void executeScheduled() {
        log.info("Trigger automático do scheduler iniciado");
        run();
    }

    public void executeManual() {
        log.info("Trigger manual iniciado via API");
        run();
    }

    private void run() {
        List<Member> members = memberRepository.findByActiveTrue();

        if (members.isEmpty()) {
            log.warn("Nenhum membro ativo encontrado. Nenhum e-mail enviado.");
            return;
        }

        for (Member member : members) {
            boolean success = emailService.sendReminder(member);
            BillingStatus status = success ? BillingStatus.SENT : BillingStatus.FAILED;
            String error = success ? null : "Falha no envio — ver logs para detalhes";
            billingRecordRepository.save(new BillingRecord(member, status, error));
        }

        log.info("Job concluído. {} e-mail(s) processado(s).", members.size());
    }
}
