package com.autotrack.service;

import com.autotrack.model.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExpiryReminderScheduler {

    @Autowired
    private FirebaseService firebaseService;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public static final java.util.Set<String> activeUsers = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private final Map<String, String> emailSentLog = new ConcurrentHashMap<>();

    // Run every minute (60000 ms) in the background
    @PostConstruct
    @Scheduled(fixedRate = 60000)
    public void checkAndSendExpiryReminders() {
        if (mailSender == null) {
            System.err.println("MailSender is not configured. Setup your spring.mail properties.");
            return;
        }

        List<Vehicle> vehicles = firebaseService.getAllVehicles();
        LocalDate today = LocalDate.now();
        String todayStr = today.toString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Vehicle vehicle : vehicles) {
            if (vehicle.getUserEmail() == null || vehicle.getUserEmail().isBlank())
                continue;
            if (!activeUsers.contains(vehicle.getUserEmail()))
                continue;

            try {
                // Check missing RC Book Image
                if (vehicle.getRcBookImageBase64() == null || vehicle.getRcBookImageBase64().isBlank()) {
                    String logKey = vehicle.getId() + "_MISSING_RC";
                    if (!todayStr.equals(emailSentLog.get(logKey))) {
                        sendEmail(vehicle.getUserEmail(), "Urgent: Action Required - Upload RC Book",
                                "Please upload your RC Book image immediately. Failure to do so will result in the deletion of your vehicle from the system.");
                        emailSentLog.put(logKey, todayStr);
                    }
                }

                // Check PUC Details
                if (vehicle.getPucDetails() != null && !vehicle.getPucDetails().isBlank()) {
                    LocalDate pucDate = LocalDate.parse(vehicle.getPucDetails(), formatter);
                    long daysToPuc = ChronoUnit.DAYS.between(today, pucDate);
                    String logKey = vehicle.getId() + "_PUC";

                    if (!todayStr.equals(emailSentLog.get(logKey))) {
                        if (daysToPuc <= 5 && daysToPuc > 0) {
                            sendEmail(vehicle.getUserEmail(), "Reminder: PUC Expiring Soon",
                                    "Your PUC for vehicle " + vehicle.getVehicleNo() + " is expiring in " + daysToPuc
                                            + " days on " + pucDate + ".");
                            emailSentLog.put(logKey, todayStr);
                        } else if (daysToPuc <= 0) {
                            sendEmail(vehicle.getUserEmail(), "Alert: PUC Expired",
                                    "Your PUC for vehicle " + vehicle.getVehicleNo()
                                            + " has expired. Please renew it immediately.");
                            emailSentLog.put(logKey, todayStr);
                        }
                    }
                }

                // Check Insurance Details
                if (vehicle.getInsuranceDetails() != null && !vehicle.getInsuranceDetails().isBlank()) {
                    LocalDate insDate = LocalDate.parse(vehicle.getInsuranceDetails(), formatter);
                    long daysToIns = ChronoUnit.DAYS.between(today, insDate);
                    String logKey = vehicle.getId() + "_INS";

                    if (!todayStr.equals(emailSentLog.get(logKey))) {
                        if (daysToIns <= 5 && daysToIns > 0) {
                            sendEmail(vehicle.getUserEmail(), "Reminder: Insurance Expiring Soon",
                                    "Your Insurance for vehicle " + vehicle.getVehicleNo() + " is expiring in "
                                            + daysToIns + " days on " + insDate + ".");
                            emailSentLog.put(logKey, todayStr);
                        } else if (daysToIns <= 0) {
                            sendEmail(vehicle.getUserEmail(), "Alert: Insurance Expired",
                                    "Your Insurance for vehicle " + vehicle.getVehicleNo()
                                            + " has expired. Please renew it immediately.");
                            emailSentLog.put(logKey, todayStr);
                        }
                    }
                }

            } catch (Exception e) {
                System.err.println("Error parsing date for vehicle " + vehicle.getVehicleNo() + ": " + e.getMessage());
            }
        }
    }

    private void sendEmail(String toEmail, String subject, String body) {
        try {
            jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(
                    mimeMessage, "utf-8");

            String htmlMsg = "<div style='font-family: Arial, sans-serif; padding: 20px; background-color: #f8f9fc;'>"
                    + "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 4px 12px rgba(0,0,0,0.05);'>"
                    + "<h2 style='color: #4e73df; text-align: center; margin-bottom: 5px;'>Autotrack Notification</h2>"
                    + "<p style='text-align: center; color: #858796; font-size: 14px; margin-top: 0;'>Automated Vehicle Alert</p>"
                    + "<hr style='border: 1px solid #eaecf4; margin: 20px 0;'/>"
                    + "<p style='font-size: 16px; color: #5a5c69; line-height: 1.6; padding: 10px 0;'>"
                    + body.replace("\n", "<br/>") + "</p>"
                    + "<div style='margin-top: 20px; text-align: center;'>"
                    + "<a href='http://localhost:8082/login' style='display: inline-block; background-color: #4e73df; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;'>Login to Dashboard</a>"
                    + "</div>"
                    + "<p style='font-size: 12px; color: #b7b9cc; text-align: center; margin-top: 30px;'>"
                    + "Thank you for using Autotrack.<br/>Please do not reply to this automated message."
                    + "</p>"
                    + "</div></div>";

            helper.setText(htmlMsg, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setFrom("autotrack.noreply@gmail.com");

            mailSender.send(mimeMessage);
            System.out.println("HTML Email reminder sent to: " + toEmail + " regarding " + subject);
        } catch (Exception e) {
            System.err.println("Failed to send HTML email to " + toEmail + ": " + e.getMessage());
        }
    }
}
