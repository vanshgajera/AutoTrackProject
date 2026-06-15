package com.autotrack.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogoutController {

    @GetMapping({ "/admin/logout", "/logout" })
    public String logout(HttpSession session) {
        String email = (String) session.getAttribute("loggedInUser");
        if (email != null) {
            com.autotrack.service.ExpiryReminderScheduler.activeUsers.remove(email);
        }

        session.invalidate(); // 🔥 session clear

        return "redirect:/login"; // 🔥 login page
    }
}