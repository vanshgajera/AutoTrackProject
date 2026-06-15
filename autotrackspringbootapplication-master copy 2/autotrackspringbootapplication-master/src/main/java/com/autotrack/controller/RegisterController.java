package com.autotrack.controller;

import com.autotrack.model.User;
import com.autotrack.service.FirebaseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user") // ✅ separate base path
public class RegisterController {

    @Autowired
    private FirebaseService firebaseService;

    // ✅ REGISTER API
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {

        try {
            String result = firebaseService.registerUser(user);

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            e.printStackTrace();
            return ResponseEntity
                    .status(500)
                    .body("Registration Failed: " + e.getMessage());
        }
    }
}