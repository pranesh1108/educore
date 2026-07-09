package com.cts.audit;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.cts.annotation.AuditEvent;
import com.cts.entity.AuditLog;
import com.cts.repository.AuditLogRepository;
import lombok.AllArgsConstructor;

@Aspect
@Component
@AllArgsConstructor
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    @AfterReturning("@annotation(auditEvent)")
    public void logAudit(AuditEvent auditEvent) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String doneBy = (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getName()))
                ? auth.getName()
                : "anonymous";

        AuditLog log = new AuditLog();
        log.setEventName(auditEvent.eventName());
        log.setEventType(auditEvent.eventType());
        log.setEventMessage(auditEvent.eventMessage());
        log.setDoneBy(doneBy);
        auditLogRepository.save(log);
    }
}