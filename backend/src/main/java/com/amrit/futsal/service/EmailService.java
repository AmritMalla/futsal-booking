package com.amrit.futsal.service;

import com.amrit.futsal.entity.Booking;
import com.amrit.futsal.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@futsalbook.com}")
    private String fromEmail;

    @Value("${app.name:FutsalBook}")
    private String appName;

    private boolean isEmailEnabled() {
        return mailSender != null;
    }

    @Async
    public void sendBookingConfirmation(Booking booking, User user) {
        if (!isEmailEnabled()) {
            logger.info("Email service not configured. Skipping booking confirmation email to: {}", user.getEmail());
            return;
        }

        String subject = String.format("Booking Confirmed - %s", booking.getGround().getName());
        String body = buildBookingConfirmationEmail(booking, user);

        sendHtmlEmail(user.getEmail(), subject, body);
    }

    @Async
    public void sendBookingCancellation(Booking booking, User user) {
        if (!isEmailEnabled()) {
            logger.info("Email service not configured. Skipping booking cancellation email to: {}", user.getEmail());
            return;
        }

        String subject = String.format("Booking Cancelled - %s", booking.getGround().getName());
        String body = buildBookingCancellationEmail(booking, user);

        sendHtmlEmail(user.getEmail(), subject, body);
    }

    @Async
    public void sendWelcomeEmail(User user) {
        if (!isEmailEnabled()) {
            logger.info("Email service not configured. Skipping welcome email to: {}", user.getEmail());
            return;
        }

        String subject = String.format("Welcome to %s!", appName);
        String body = buildWelcomeEmail(user);

        sendHtmlEmail(user.getEmail(), subject, body);
    }

    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        if (!isEmailEnabled()) {
            logger.info("Email service not configured. Skipping password reset email to: {}", user.getEmail());
            return;
        }

        String subject = "Password Reset Request";
        String body = buildPasswordResetEmail(user, resetToken);

        sendHtmlEmail(user.getEmail(), subject, body);
    }

    @Async
    public void sendBookingReminder(Booking booking, User user) {
        if (!isEmailEnabled()) {
            logger.info("Email service not configured. Skipping booking reminder email to: {}", user.getEmail());
            return;
        }

        String subject = String.format("Reminder: Your booking at %s is coming up!", booking.getGround().getName());
        String body = buildBookingReminderEmail(booking, user);

        sendHtmlEmail(user.getEmail(), subject, body);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send email to: {}", to, e);
        }
    }

    private void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send email to: {}", to, e);
        }
    }

    private String buildBookingConfirmationEmail(Booking booking, User user) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #2E7D32, #4CAF50); color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 20px; border-radius: 0 0 10px 10px; }
                    .booking-details { background: white; padding: 15px; border-radius: 8px; margin: 15px 0; }
                    .detail-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #eee; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    .btn { display: inline-block; background: #FF6B00; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; margin-top: 15px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⚽ Booking Confirmed!</h1>
                    </div>
                    <div class="content">
                        <p>Hi <strong>%s</strong>,</p>
                        <p>Great news! Your futsal arena booking has been confirmed. Here are the details:</p>

                        <div class="booking-details">
                            <div class="detail-row">
                                <span>Ground:</span>
                                <strong>%s</strong>
                            </div>
                            <div class="detail-row">
                                <span>Date:</span>
                                <strong>%s</strong>
                            </div>
                            <div class="detail-row">
                                <span>Time:</span>
                                <strong>%s - %s</strong>
                            </div>
                            <div class="detail-row">
                                <span>Booking ID:</span>
                                <strong>%s</strong>
                            </div>
                        </div>

                        <p>Please arrive 10 minutes before your scheduled time.</p>
                        <p>Have a great game!</p>

                        <p>Best regards,<br>The %s Team</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 %s. All rights reserved.</p>
                        <p>If you have any questions, contact us at support@futsalbook.com</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            user.getName(),
            booking.getGround().getName(),
            booking.getSlot().getStartTime().format(dateFormatter),
            booking.getSlot().getStartTime().format(timeFormatter),
            booking.getSlot().getEndTime().format(timeFormatter),
            booking.getId().toString().substring(0, 8).toUpperCase(),
            appName,
            appName
        );
    }

    private String buildBookingCancellationEmail(Booking booking, User user) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #F44336; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 20px; border-radius: 0 0 10px 10px; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    .btn { display: inline-block; background: #2E7D32; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; margin-top: 15px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Booking Cancelled</h1>
                    </div>
                    <div class="content">
                        <p>Hi <strong>%s</strong>,</p>
                        <p>Your booking at <strong>%s</strong> for <strong>%s</strong> has been cancelled.</p>
                        <p>If you didn't request this cancellation, please contact our support team immediately.</p>
                        <p>We hope to see you on the pitch soon!</p>
                        <a href="#" class="btn">Book Another Slot</a>
                        <p>Best regards,<br>The %s Team</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            user.getName(),
            booking.getGround().getName(),
            booking.getSlot().getStartTime().format(dateFormatter),
            appName,
            appName
        );
    }

    private String buildWelcomeEmail(User user) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #2E7D32, #4CAF50); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 20px; border-radius: 0 0 10px 10px; }
                    .feature { background: white; padding: 15px; border-radius: 8px; margin: 10px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    .btn { display: inline-block; background: #FF6B00; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; margin-top: 15px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⚽ Welcome to %s!</h1>
                        <p>Your journey to easier futsal arena booking starts here</p>
                    </div>
                    <div class="content">
                        <p>Hi <strong>%s</strong>,</p>
                        <p>Welcome to %s! We're thrilled to have you on board.</p>

                        <div class="feature">
                            <strong>🏟️ Browse Grounds</strong>
                            <p>Discover the best futsal grounds in your area</p>
                        </div>

                        <div class="feature">
                            <strong>📅 Easy Booking</strong>
                            <p>Book your preferred time slots in seconds</p>
                        </div>

                        <div class="feature">
                            <strong>🔒 Secure Payments</strong>
                            <p>Safe and encrypted payment processing</p>
                        </div>

                        <center><a href="#" class="btn">Browse Grounds Now</a></center>

                        <p>Best regards,<br>The %s Team</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            appName,
            user.getName(),
            appName,
            appName,
            appName
        );
    }

    private String buildPasswordResetEmail(User user, String resetToken) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #2E7D32; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 20px; border-radius: 0 0 10px 10px; }
                    .code { background: white; padding: 20px; text-align: center; font-size: 24px; font-family: monospace; letter-spacing: 5px; border-radius: 8px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Password Reset Request</h1>
                    </div>
                    <div class="content">
                        <p>Hi <strong>%s</strong>,</p>
                        <p>We received a request to reset your password. Use the code below to reset it:</p>

                        <div class="code">%s</div>

                        <p>This code will expire in 15 minutes.</p>
                        <p>If you didn't request a password reset, please ignore this email.</p>

                        <p>Best regards,<br>The %s Team</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            user.getName(),
            resetToken,
            appName,
            appName
        );
    }

    private String buildBookingReminderEmail(Booking booking, User user) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #FF9800; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 20px; border-radius: 0 0 10px 10px; }
                    .booking-details { background: white; padding: 15px; border-radius: 8px; margin: 15px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⏰ Reminder: Game Time Soon!</h1>
                    </div>
                    <div class="content">
                        <p>Hi <strong>%s</strong>,</p>
                        <p>Just a friendly reminder that your futsal arena booking is coming up!</p>

                        <div class="booking-details">
                            <p><strong>Ground:</strong> %s</p>
                            <p><strong>Date:</strong> %s</p>
                            <p><strong>Time:</strong> %s - %s</p>
                        </div>

                        <p>Please arrive 10 minutes early to warm up!</p>
                        <p>Have a great game! ⚽</p>

                        <p>Best regards,<br>The %s Team</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            user.getName(),
            booking.getGround().getName(),
            booking.getSlot().getStartTime().format(dateFormatter),
            booking.getSlot().getStartTime().format(timeFormatter),
            booking.getSlot().getEndTime().format(timeFormatter),
            appName,
            appName
        );
    }
}
