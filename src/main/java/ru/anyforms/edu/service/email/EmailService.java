package ru.anyforms.edu.service.email;

public interface EmailService {

    void sendEmail(String to, String subject, String htmlBody);
}
