package com.example.autotrackspringboot;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {

    @RequestMapping("/")
    public String index() {
        return "index";
    }
    @PostMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}
