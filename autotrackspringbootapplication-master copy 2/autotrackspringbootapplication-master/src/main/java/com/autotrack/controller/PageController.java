package com.autotrack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String indexPage() {
        return "register";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }


    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

//    @GetMapping("/dashboard")
//    public String dashboard() {
//        return "dashboard"; // dashboard.html
//    }



}