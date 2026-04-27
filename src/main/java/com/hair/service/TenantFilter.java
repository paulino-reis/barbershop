package com.hair.service;

import com.hair.repository.TenantConfigRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@RequiredArgsConstructor
public class TenantFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(TenantFilter.class);

    private final TenantConfigRepository tenantConfigRepository;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("TenantFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String host = httpRequest.getServerName();
        String[] parts = host.split("\\.");

        log.debug("TenantFilter: Host = {}, Parts = {}", host, parts.length);

        if (host.equals("localhost") || host.equals("127.0.0.1")) {
            // Para localhost, assume tenant_id = 4
            TenantContext.setCurrentTenantId(4);
            log.debug("TenantFilter: Set tenant ID = 4 for localhost");
        } else if (parts.length >= 2) {
            // Pega 'talison' de 'talison.smartsalao.com.br'
            String slug = parts[0];
            TenantContext.setCurrentTenant(slug);
            log.debug("TenantFilter: Set tenant slug = {}", slug);

            // Buscar o tenant_id (Integer) da tabela tenant_config
            tenantConfigRepository.findBySlug(slug).ifPresent(tenantConfig -> {
                TenantContext.setCurrentTenantId(tenantConfig.getTenantId());
                log.debug("TenantFilter: Set tenant ID = {}", tenantConfig.getTenantId());
            });

            if (TenantContext.getCurrentTenantId() == null) {
                log.warn("TenantFilter: No tenant config found for slug = {}, setting tenant ID = 4", slug);
                TenantContext.setCurrentTenantId(4);
            }
        } else {
            log.warn("TenantFilter: Invalid host format, expected at least 2 parts");
        }

        // Se tenant_id ainda for null, set para 4
        if (TenantContext.getCurrentTenantId() == null) {
            TenantContext.setCurrentTenantId(4);
            log.debug("TenantFilter: tenant_id was null, set to 4");
        }

        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        log.info("TenantFilter destroyed");
    }
}
