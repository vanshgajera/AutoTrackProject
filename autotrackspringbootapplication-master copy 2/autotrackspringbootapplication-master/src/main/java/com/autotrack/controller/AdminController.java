package com.autotrack.controller;

import com.autotrack.model.User;
import com.autotrack.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // ---------------- USERS PAGE ----------------
    @GetMapping("/users")
    public String listUsers(@RequestParam(name = "keyword", required = false) String keyword, Model model) {

        System.out.println("Loading users...");

        List<User> users = adminService.searchUsers(keyword);

        System.out.println("Users loaded: " + users.size());

        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);

        return "admin/users";
    }

    // ---------------- DELETE USER ----------------
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable String id) {
        adminService.deleteUser(id);
        return "redirect:/admin/users";
    }

    // ---------------- VEHICLES PAGE ----------------
    @GetMapping("/vehicles")
    public String listVehicles(@RequestParam(name = "keyword", required = false) String keyword, Model model) {
        List<com.autotrack.model.Vehicle> vehicles = adminService.searchVehicles(keyword);
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("keyword", keyword);
        return "admin/vehicles";
    }

    // ---------------- DELETE VEHICLE ----------------
    @GetMapping("/vehicles/delete/{id}")
    public String deleteAdminVehicle(@PathVariable String id) {
        adminService.deleteVehicle(id);
        return "redirect:/admin/vehicles";
    }
}