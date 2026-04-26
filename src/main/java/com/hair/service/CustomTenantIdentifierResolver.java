package com.hair.service;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class CustomTenantIdentifierResolver implements CurrentTenantIdentifierResolver<Integer> {
    @Override
    public Integer resolveCurrentTenantIdentifier() {
        return TenantContext.getCurrentTenantId();
    }

    @Override
    public boolean validateExistingCurrentSessions() { return true; }
}
