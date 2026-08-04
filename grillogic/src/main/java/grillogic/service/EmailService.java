package grillogic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(String toEmail, String tempPassword) {
        System.out.println(">>> ATTEMPTING TO SEND EMAIL TO: [" + toEmail + "]");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("smertz@getgrillogic.com");
        message.setTo(toEmail);
        message.setSubject("Welcome to GRILLOGIC — Your Account Is Ready");
        message.setText(
                "Thanks for your payment! Your GRILLOGIC account has been created.\n\n" +
                        "Login at: https://getgrillogic.com/login\n" +
                        "Email: " + toEmail + "\n" +
                        "Temporary Password: " + tempPassword + "\n\n" +
                        "We recommend logging in and updating this as soon as possible.\n\n" +
                        "— Scott Mertz, GRILLOGIC"
        );

        mailSender.send(message);

        System.out.println(">>> EMAIL SEND CALL COMPLETED — no exception thrown, SMTP server accepted it.");
    }
}