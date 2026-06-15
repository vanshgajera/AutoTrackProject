package com.autotrack.model;

public class User {

    private String id;
    private String name;  // ✅ Add this
    private String email;
    private String password;

    // Default constructor (Firebase needs)
    public User() {}

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; } // ✅ Add getter
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}