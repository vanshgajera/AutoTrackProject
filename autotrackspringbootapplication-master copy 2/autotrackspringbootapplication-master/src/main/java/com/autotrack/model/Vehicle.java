package com.autotrack.model;

public class Vehicle {

    private String id;
    private String vehicleNo;
    private String ownerName;
    private String pucDetails;
    private String insuranceDetails;
    private String userEmail;
    private String status;
    private String expiryMessage;
    private String rcBookImageBase64;

    // Default constructor (Firebase needs)
    public Vehicle() {}

    public String getExpiryMessage() { return expiryMessage; }
    public void setExpiryMessage(String expiryMessage) { this.expiryMessage = expiryMessage; }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getVehicleNo() { return vehicleNo; }
    public void setVehicleNo(String vehicleNo) { this.vehicleNo = vehicleNo; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getPucDetails() { return pucDetails; }
    public void setPucDetails(String pucDetails) { this.pucDetails = pucDetails; }

    public String getInsuranceDetails() { return insuranceDetails; }
    public void setInsuranceDetails(String insuranceDetails) { this.insuranceDetails = insuranceDetails; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRcBookImageBase64() { return rcBookImageBase64; }
    public void setRcBookImageBase64(String rcBookImageBase64) { this.rcBookImageBase64 = rcBookImageBase64; }
}
