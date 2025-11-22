package com.ecom.service;

import com.ecom.dto.TenantDashboardDTO;

/**
 * Service interface for Tenant Dashboard operations
 * Aggregates all tenant-related information
 * Follows Interface Segregation Principle
 */
public interface TenantDashboardService {

    /**
     * Get complete dashboard data for a tenant
     * Aggregates rental, payment, and communication information
     */
    TenantDashboardDTO getDashboardData(Integer tenantId);

    /**
     * Check if tenant has any active rentals
     */
    boolean hasActiveRental(Integer tenantId);

    /**
     * Get active rental booking ID for tenant
     */
    Integer getActiveBookingId(Integer tenantId);
}
