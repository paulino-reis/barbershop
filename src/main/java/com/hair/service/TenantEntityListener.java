package com.hair.service;

import com.hair.model.BaseEntity;
import jakarta.persistence.PrePersist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Listener para preencher o tenant_id automaticamente no save()
public class TenantEntityListener {
    private static final Logger log = LoggerFactory.getLogger(TenantEntityListener.class);

    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof BaseEntity baseEntity) {
            Integer tenantId = TenantContext.getCurrentTenantId();
            log.info("TenantEntityListener: Setting tenantId = {} for entity = {}", tenantId, entity.getClass().getSimpleName());
            baseEntity.setTenantId(tenantId);
        }
    }
}
