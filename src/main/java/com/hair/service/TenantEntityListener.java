package com.hair.service;

import com.hair.model.BaseEntity;
import jakarta.persistence.PrePersist;

// Listener para preencher o tenant_id automaticamente no save()
public class TenantEntityListener {

    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof BaseEntity baseEntity) {
            Integer tenantId = TenantContext.getCurrentTenantId();
            baseEntity.setTenantId(tenantId);
        }
    }
}
