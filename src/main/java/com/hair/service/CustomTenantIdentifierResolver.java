package com.hair.service;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class CustomTenantIdentifierResolver implements CurrentTenantIdentifierResolver<Integer> {
    private static final Integer DEFAULT_TENANT = 4;

    @Override
    public Integer resolveCurrentTenantIdentifier() {
        Integer tenantId = TenantContext.getCurrentTenantId();
        return tenantId != null ? tenantId : DEFAULT_TENANT;
    }

    @Override
    public boolean validateExistingCurrentSessions() { return true; }
}
