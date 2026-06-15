package com.autotrack.controller;

import com.autotrack.model.User;
import com.autotrack.model.Vehicle;
import com.autotrack.service.FirebaseService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Base64;

import java.util.List;

@Controller
public class UserController {

    @Autowired
    private FirebaseService firebaseService;

    // USER DASHBOARD
    @GetMapping("/dashboard")
    public String userDashboard(HttpSession session, Model model) {
        String email = (String) session.getAttribute("loggedInUser");
        if (email == null)
            return "redirect:/login";

        User user = firebaseService.getUserDetails(email);
        List<Vehicle> vehicles = firebaseService.getVehiclesByUserEmail(email);

        List<Vehicle> deactiveVehicles = vehicles.stream()
                .filter(v -> "Deactive".equalsIgnoreCase(v.getStatus()))
                .toList();

        List<Vehicle> pucExpiredVehicles = vehicles.stream()
                .filter(v -> "Deactive".equalsIgnoreCase(v.getStatus()) && v.getExpiryMessage() != null && v.getExpiryMessage().contains("PUC"))
                .toList();

        model.addAttribute("user", user);
        model.addAttribute("totalVehicles", vehicles.size());
        model.addAttribute("deactiveVehicles", deactiveVehicles);
        model.addAttribute("pucExpiredVehicles", pucExpiredVehicles);

        return "user-panel";
    }

    // ADD VEHICLE - VIEW
    @GetMapping("/add-vehicle")
    public String addVehiclePage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("loggedInUser");
        if (email == null)
            return "redirect:/login";

        model.addAttribute("userEmail", email);
        return "add-vehicle";
    }

    // ADD VEHICLE - POST
    @PostMapping("/add-vehicle")
    public String addVehicle(HttpSession session, Vehicle vehicle,
                             @RequestParam(value = "rcBookImageFile", required = false) MultipartFile rcBookImageFile) {
        String email = (String) session.getAttribute("loggedInUser");
        if (email == null)
            return "redirect:/login";

        vehicle.setUserEmail(email);

        try {
            if (rcBookImageFile != null && !rcBookImageFile.isEmpty()) {
                String base64 = Base64.getEncoder().encodeToString(rcBookImageFile.getBytes());
                vehicle.setRcBookImageBase64("data:" + rcBookImageFile.getContentType() + ";base64," + base64);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        firebaseService.addVehicle(vehicle);

        return "redirect:/view-vehicles";
    }

    // VIEW VEHICLES
    @GetMapping("/view-vehicles")
    public String viewVehicles(HttpSession session, Model model) {
        String email = (String) session.getAttribute("loggedInUser");
        if (email == null)
            return "redirect:/login";

        List<Vehicle> vehicles = firebaseService.getVehiclesByUserEmail(email);
        model.addAttribute("vehicles", vehicles);

        return "view-vehicles";
    }

    // EDIT VEHICLE - VIEW
    @GetMapping("/edit-vehicle/{id}")
    public String editVehiclePage(@PathVariable("id") String id, HttpSession session, Model model) {
        String email = (String) session.getAttribute("loggedInUser");
        if (email == null)
            return "redirect:/login";

        Vehicle vehicle = firebaseService.getVehicleById(id);
        if (vehicle == null || !vehicle.getUserEmail().equals(email)) {
            return "redirect:/view-vehicles"; // Not found or not authorized
        }

        model.addAttribute("vehicle", vehicle);
        return "edit-vehicle";
    }

    // UPDATE VEHICLE - POST
    @PostMapping("/update-vehicle")
    public String updateVehicle(HttpSession session, Vehicle vehicle,
                                @RequestParam(value = "rcBookImageFile", required = false) MultipartFile rcBookImageFile) {
        String email = (String) session.getAttribute("loggedInUser");
        if (email == null)
            return "redirect:/login";

        vehicle.setUserEmail(email);

        try {
            Vehicle existingVehicle = firebaseService.getVehicleById(vehicle.getId());
            if (rcBookImageFile != null && !rcBookImageFile.isEmpty()) {
                String base64 = Base64.getEncoder().encodeToString(rcBookImageFile.getBytes());
                vehicle.setRcBookImageBase64("data:" + rcBookImageFile.getContentType() + ";base64," + base64);
            } else if (existingVehicle != null) {
                vehicle.setRcBookImageBase64(existingVehicle.getRcBookImageBase64());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        firebaseService.updateVehicle(vehicle.getId(), vehicle);

        return "redirect:/view-vehicles";
    }

    // DELETE VEHICLE
    @GetMapping("/delete-vehicle/{id}")
    public String deleteVehicle(@PathVariable("id") String id, HttpSession session) {
        String email = (String) session.getAttribute("loggedInUser");
        if (email == null)
            return "redirect:/login";

        firebaseService.deleteVehicle(id);
        return "redirect:/view-vehicles";
    }
}
