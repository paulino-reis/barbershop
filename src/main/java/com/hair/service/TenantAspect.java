package com.hair.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantAspect {

    private static final Logger logger = LoggerFactory.getLogger(TenantAspect.class);
    private static final ThreadLocal<Boolean> isExecuting = ThreadLocal.withInitial(() -> false);

    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* com.hair.service.*.*(..)) && !within(TenantAspect) && !within(TenantService)")
    public void beforeExecution() {
        if (Boolean.TRUE.equals(isExecuting.get())) {
            return;
        }

        isExecuting.set(true);
        try {
            Integer tenantId = TenantContext.getCurrentTenantId();
            if (tenantId != null) {
                try {
                    Session session = entityManager.unwrap(Session.class);
                    session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
                } catch (Exception e) {
                    logger.error("Failed to enable tenant filter for tenantId: {}", tenantId, e);
                }
            }
        } finally {
            isExecuting.remove();
        }
    }
}
