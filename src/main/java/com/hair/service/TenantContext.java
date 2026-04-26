package com.hair.service;

public class TenantContext {
    private TenantContext() {
    }

    private static final ThreadLocal<String> currentTenantSlug = new ThreadLocal<>();
    private static final ThreadLocal<Integer> currentTenantId = new ThreadLocal<>();

    public static void setCurrentTenant(String tenantSlug) {
        currentTenantSlug.set(tenantSlug);
    }

    public static void setCurrentTenantId(Integer tenantId) {
        currentTenantId.set(tenantId);
    }

    public static String getCurrentTenant() {
        return currentTenantSlug.get();
    }

    public static Integer getCurrentTenantId() {
        return currentTenantId.get();
    }

    public static void clear() {
        currentTenantSlug.remove();
        currentTenantId.remove();
    }
}