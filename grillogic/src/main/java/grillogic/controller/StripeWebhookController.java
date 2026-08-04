package grillogic.controller;

import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import grillogic.model.User;
import grillogic.repository.UserRepository;
import grillogic.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;

@RestController
public class StripeWebhookController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Autowired
    public StripeWebhookController(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder,
                                   EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @PostMapping("/api/stripe/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            try {
                Session session = (Session) event.getDataObjectDeserializer().deserializeUnsafe();

                // customer_email is only set when pre-filled programmatically.
                // For a real buyer typing their email at checkout, it lands in
                // customer_details.email instead — check both, prefer customer_details.
                String email = null;
                if (session.getCustomerDetails() != null) {
                    email = session.getCustomerDetails().getEmail();
                }
                if (email == null) {
                    email = session.getCustomerEmail();
                }

                String mode = session.getMode();

                if (email != null) {
                    handleSuccessfulCheckout(email, mode);
                }
            } catch (EventDataObjectDeserializationException e) {
                return ResponseEntity.badRequest().body("Could not deserialize checkout session: " + e.getMessage());
            }
        }

        return ResponseEntity.ok("Received");
    }

    private void handleSuccessfulCheckout(String email, String mode) {
        User user = userRepository.findByEmail(email).orElse(null);
        boolean isNewUser = (user == null);

        if (isNewUser) {
            user = new User();
            user.setEmail(email);
            user.setRole("CLIENT");

            String tempPassword = generateTempPassword();
            user.setPasswordHash(passwordEncoder.encode(tempPassword));

            applyTier(user, mode);
            userRepository.save(user);

            emailService.sendWelcomeEmail(email, tempPassword);
        } else {
            applyTier(user, mode);
            userRepository.save(user);
        }
    }

    private void applyTier(User user, String mode) {
        if ("subscription".equals(mode)) {
            user.setTier("PREMIUM");
            user.setSubscriptionActive(true);
        } else {
            if (user.getTier() == null || "NONE".equals(user.getTier())) {
                user.setTier("AUDIT_ONLY");
            }
        }
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}