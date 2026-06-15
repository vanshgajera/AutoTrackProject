package com.autotrack.controller;

import com.autotrack.service.AdminService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final AdminService adminService;

    public DashboardController(AdminService adminService) {
        this.adminService = adminService;
    }

    // 🔥 MAIN DASHBOARD (DATA YAHI SE AATA HAI)
    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {

        long totalUsers = adminService.countUsers();
        long totalVehicles = adminService.countVehicles();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalVehicles", totalVehicles);
        model.addAttribute("pendingRegistrations", adminService.countPendingRegistrations());
        model.addAttribute("approvedRegistrations", adminService.countApprovedRegistrations());
        model.addAttribute("activeVehicles", adminService.countActiveVehicles());
        model.addAttribute("deactiveVehicles", adminService.countDeactiveVehicles());

        return "dashboard";
    }

    // 🔥 REDIRECT
    // @GetMapping("/dashboard")
    // public String redirectDashboard() {
    // return "redirect:/admin/dashboard"; // ✅ fix
    // }

    // @GetMapping("/dashboard")
    // public String dashboard() {
    // return "dashboard"; // dashboard.html
    // }
}