package com.mecanico.vehiclepassport.controller;

import com.mecanico.vehiclepassport.dto.DashboardDtos;
import com.mecanico.vehiclepassport.security.CurrentUser;
import com.mecanico.vehiclepassport.service.DashboardService;
import com.mecanico.vehiclepassport.service.NotificationScanService;
import com.mecanico.vehiclepassport.service.VehicleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardService dashboardService;
    private final NotificationScanService scanService;
    private final VehicleService vehicleService;
    private final CurrentUser currentUser;

    public DashboardController(DashboardService dashboardService,
                               NotificationScanService scanService,
                               VehicleService vehicleService,
                               CurrentUser currentUser) {
        this.dashboardService = dashboardService;
        this.scanService = scanService;
        this.vehicleService = vehicleService;
        this.currentUser = currentUser;
    }

    @GetMapping("/dashboard/summary")
    public DashboardDtos.DashboardSummaryResponse summary() {
        return dashboardService.summary();
    }

    @GetMapping("/notifications")
    public List<DashboardDtos.NotificationResponse> notifications() {
        return dashboardService.recentNotifications();
    }

    /**
     * Manual trigger for the daily cron - useful for the demo and for testing.
     */
    @PostMapping("/admin/scan")
    @PreAuthorize("hasRole('ADMIN')")
    public DashboardDtos.ScanResultResponse scan() {
        return scanService.scanNow();
    }

    @GetMapping("/me/vehicle-ids")
    public java.util.Set<Long> myVehicleIds() {
        return vehicleService.accessibleVehicleIds();
    }
}
