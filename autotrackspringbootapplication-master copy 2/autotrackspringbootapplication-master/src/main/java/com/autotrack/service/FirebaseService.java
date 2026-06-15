package com.autotrack.service;

import com.autotrack.model.User;
import com.google.firebase.database.*;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

@Service
public class FirebaseService {

    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    // ===== REGISTER USER =====
    public String registerUser(User user) {

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return "Error: Email cannot be empty";
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            return "Error: Password cannot be empty";
        }

        String safeEmail = user.getEmail().replace(".", "_");

        try {
            dbRef.child("users").child(safeEmail).setValueAsync(user).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "User Registered Successfully";
    }

    // ===== LOGIN USER =====
    public CompletableFuture<Boolean> loginUser(String email, String password) {

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        if (email == null || email.isBlank()) {
            future.completeExceptionally(new IllegalArgumentException("Email cannot be empty"));
            return future;
        }

        if (password == null || password.isBlank()) {
            future.completeExceptionally(new IllegalArgumentException("Password cannot be empty"));
            return future;
        }

        String safeEmail = email.replace(".", "_");

        dbRef.child("users").child(safeEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            String dbPassword = snapshot.child("password").getValue(String.class);

                            if (dbPassword != null && dbPassword.equals(password)) {
                                future.complete(true);
                            } else {
                                future.complete(false);
                            }

                        } else {
                            future.complete(false);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        future.completeExceptionally(
                                new RuntimeException("Firebase error: " + error.getMessage()));
                    }
                });

        return future;
    }

    // ===== GET USER DETAILS =====
    public User getUserDetails(String email) {
        if (email == null || email.isBlank())
            return null;
        String safeEmail = email.replace(".", "_");

        final User[] userArr = { null };
        CountDownLatch latch = new CountDownLatch(1);

        dbRef.child("users").child(safeEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userArr[0] = snapshot.getValue(User.class);
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return userArr[0];
    }

    // ===== CHECK EXPIRY DATES =====
    private void checkAndApplyDeactivation(com.autotrack.model.Vehicle vehicle) {
        if (vehicle == null)
            return;
        try {
            java.time.LocalDate today = java.time.LocalDate.now();
            boolean pucExpired = false;
            boolean insExpired = false;
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

            if (vehicle.getPucDetails() != null) {
                java.util.regex.Matcher m = pattern.matcher(vehicle.getPucDetails());
                if (m.find()) {
                    java.time.LocalDate date = java.time.LocalDate.parse(m.group());
                    if (date.isBefore(today))
                        pucExpired = true;
                }
            }
            if (vehicle.getInsuranceDetails() != null) {
                java.util.regex.Matcher m = pattern.matcher(vehicle.getInsuranceDetails());
                if (m.find()) {
                    java.time.LocalDate date = java.time.LocalDate.parse(m.group());
                    if (date.isBefore(today))
                        insExpired = true;
                }
            }
            if (pucExpired && insExpired) {
                vehicle.setStatus("Deactive");
                vehicle.setExpiryMessage("PUC & Insurance Expired");
            } else if (pucExpired) {
                vehicle.setStatus("Deactive");
                vehicle.setExpiryMessage("PUC Expired");
            } else if (insExpired) {
                vehicle.setStatus("Deactive");
                vehicle.setExpiryMessage("Insurance Expired");
            } else {
                vehicle.setExpiryMessage(null);
                // Set to Active if previously forced to Deactive by system
                if ("Deactive".equals(vehicle.getStatus())) {
                    vehicle.setStatus("Active");
                }
            }
        } catch (Exception e) {
        }
    }

    // ===== ADD VEHICLE =====
    public String addVehicle(com.autotrack.model.Vehicle vehicle) {
        String vehicleId = dbRef.child("vehicles").push().getKey();
        vehicle.setId(vehicleId);

        if (vehicle.getStatus() == null || vehicle.getStatus().isEmpty()) {
            vehicle.setStatus("Active");
        }
        checkAndApplyDeactivation(vehicle);

        try {
            dbRef.child("vehicles").child(vehicleId).setValueAsync(vehicle).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vehicleId;
    }

    // ===== GET USER'S VEHICLES =====
    public java.util.List<com.autotrack.model.Vehicle> getVehiclesByUserEmail(String email) {
        final java.util.List<com.autotrack.model.Vehicle> vehicleList = new java.util.ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        dbRef.child("vehicles").orderByChild("userEmail").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            com.autotrack.model.Vehicle v = child.getValue(com.autotrack.model.Vehicle.class);
                            if (v != null) {
                                v.setId(child.getKey()); // Ensure proper ID from node key
                                String oldStatus = v.getStatus();
                                checkAndApplyDeactivation(v);
                                if (v.getStatus() != null && !v.getStatus().equals(oldStatus)) {
                                    dbRef.child("vehicles").child(v.getId()).setValueAsync(v);
                                }
                                vehicleList.add(v);
                            }
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        latch.countDown();
                    }
                });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return vehicleList;
    }

    // ===== GET ALL VEHICLES =====
    public java.util.List<com.autotrack.model.Vehicle> getAllVehicles() {
        final java.util.List<com.autotrack.model.Vehicle> vehicleList = new java.util.ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        dbRef.child("vehicles")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            com.autotrack.model.Vehicle v = child.getValue(com.autotrack.model.Vehicle.class);
                            if (v != null) {
                                v.setId(child.getKey()); // Ensure proper ID from node key
                                checkAndApplyDeactivation(v);
                                vehicleList.add(v);
                            }
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        latch.countDown();
                    }
                });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return vehicleList;
    }

    // ===== GET VEHICLE BY ID =====
    public com.autotrack.model.Vehicle getVehicleById(String vehicleId) {
        if (vehicleId == null || vehicleId.isBlank())
            return null;
        final com.autotrack.model.Vehicle[] vehicleArr = { null };
        CountDownLatch latch = new CountDownLatch(1);

        dbRef.child("vehicles").child(vehicleId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    vehicleArr[0] = snapshot.getValue(com.autotrack.model.Vehicle.class);
                    if (vehicleArr[0] != null) {
                        vehicleArr[0].setId(snapshot.getKey());
                    }
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return vehicleArr[0];
    }

    // ===== UPDATE VEHICLE =====
    public void updateVehicle(String vehicleId, com.autotrack.model.Vehicle updatedVehicle) {
        if (vehicleId != null && !vehicleId.isBlank()) {
            updatedVehicle.setId(vehicleId); // Ensure ID is maintained
            if (updatedVehicle.getStatus() == null || updatedVehicle.getStatus().isEmpty()) {
                updatedVehicle.setStatus("Active");
            }
            checkAndApplyDeactivation(updatedVehicle);
            try {
                dbRef.child("vehicles").child(vehicleId).setValueAsync(updatedVehicle).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ===== DELETE VEHICLE =====
    public void deleteVehicle(String vehicleId) {
        if (vehicleId != null && !vehicleId.isBlank()) {
            try {
                dbRef.child("vehicles").child(vehicleId).removeValueAsync().get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ================================
    // 🔥 DASHBOARD METHODS START HERE
    // ================================

    // 🔹 COMMON COUNT METHOD
    private long getCount(String node) {

        final long[] count = { 0 };
        CountDownLatch latch = new CountDownLatch(1);

        dbRef.child(node).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                count[0] = snapshot.getChildrenCount();
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return count[0];
    }

    // 🔹 TOTAL USERS
    public long countUsers() {
        return getCount("users");
    }

    // 🔹 TOTAL VEHICLES
    public long countVehicles() {
        return getCount("vehicles");
    }

    // 🔹 COUNT BY STATUS
    public long countByStatus(String node, String status) {

        final long[] count = { 0 };
        CountDownLatch latch = new CountDownLatch(1);

        dbRef.child(node)
                .orderByChild("status")
                .equalTo(status)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        count[0] = snapshot.getChildrenCount();
                        latch.countDown();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        latch.countDown();
                    }
                });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return count[0];
    }
}