package com.autotrack.service;

import com.autotrack.model.User;
import com.google.firebase.database.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Service
public class AdminService {

    private final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    // ------------------- Admin Login -------------------
    public boolean validateAdmin(String email, String password) {
        final boolean[] valid = { false };
        final CountDownLatch latch = new CountDownLatch(1);

        dbRef.child("admins")
                .orderByChild("email")
                .equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot admin : snapshot.getChildren()) {
                            String dbPass = admin.child("password").getValue(String.class);
                            if (dbPass != null && dbPass.equals(password)) {
                                valid[0] = true;
                            }
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.out.println("Login Error: " + error.getMessage());
                        latch.countDown();
                    }
                });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return valid[0];
    }

    // ------------------- Dashboard Metrics -------------------
    private long getCount(String node) {
        final long[] count = { 0 };
        final CountDownLatch latch = new CountDownLatch(1);

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

    private long getCountByStatus(String node, String status) {
        final long[] count = { 0 };
        final CountDownLatch latch = new CountDownLatch(1);

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

    public long countUsers() {
        return getCount("users");
    }

    public long countVehicles() {
        return getCount("vehicles");
    }

    public long countPendingRegistrations() {
        return getCountByStatus("registrations", "PENDING");
    }

    public long countApprovedRegistrations() {
        return getCountByStatus("registrations", "APPROVED");
    }

    public long countActiveVehicles() {
        return getCountByStatus("vehicles", "Active");
    }

    public long countDeactiveVehicles() {
        return getCountByStatus("vehicles", "Deactive");
    }

    // ------------------- User Management -------------------
    public List<User> searchUsers(String keyword) {

        List<User> users = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        // ✅ FIX: null safe
        if (keyword == null)
            keyword = "";
        String searchKey = keyword.toLowerCase();

        dbRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for (DataSnapshot userSnap : snapshot.getChildren()) {

                    User user = userSnap.getValue(User.class);

                    if (user != null) {
                        String name = user.getName() != null ? user.getName().toLowerCase() : "";
                        String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";

                        if (searchKey.isEmpty() || name.contains(searchKey) || email.contains(searchKey)) {
                            user.setId(userSnap.getKey());
                            users.add(user);
                        }
                    }
                }

                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("User Fetch Error: " + error.getMessage());
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return users;
    }

    public void deleteUser(String id) {
        dbRef.child("users").child(id).removeValueAsync();
    }

    // ------------------- Vehicle Management -------------------
    public List<com.autotrack.model.Vehicle> searchVehicles(String keyword) {
        List<com.autotrack.model.Vehicle> vehicles = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        if (keyword == null)
            keyword = "";
        final String searchKey = keyword.toLowerCase();

        dbRef.child("vehicles").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot vSnap : snapshot.getChildren()) {
                    com.autotrack.model.Vehicle v = vSnap.getValue(com.autotrack.model.Vehicle.class);
                    if (v != null) {
                        String num = v.getVehicleNo() != null ? v.getVehicleNo().toLowerCase() : "";
                        String email = v.getUserEmail() != null ? v.getUserEmail().toLowerCase() : "";

                        if (searchKey.isEmpty() || num.contains(searchKey) || email.contains(searchKey)) {
                            v.setId(vSnap.getKey());
                            vehicles.add(v);
                        }
                    }
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("Vehicles Fetch Error: " + error.getMessage());
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return vehicles;
    }

    public void deleteVehicle(String id) {
        dbRef.child("vehicles").child(id).removeValueAsync();
    }
}