package grillogic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// Sends email via Resend's HTTPS API instead of raw SMTP. This matters specifically
// because Railway (and most cloud hosts) block outbound SMTP ports on free/hobby
// plans to prevent spam abuse — an HTTPS API call has no such restriction.
@Service
public class EmailService {

    @Value("${resend.api.key:}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendWelcomeEmail(String toEmail, String tempPassword) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("Resend API key not configured. Set resend.api.key.");
        }

        String textBody =
                "Thanks for your payment! Your GRILLOGIC account has been created.\n\n" +
                        "Login at: https://grillogic-production.up.railway.app/login\n" +
                        "Email: " + toEmail + "\n" +
                        "Temporary Password: " + tempPassword + "\n\n" +
                        "We recommend logging in and updating this as soon as possible.\n\n" +
                        "— Scott Mertz, GRILLOGIC";

        System.out.println(">>> ATTEMPTING TO SEND EMAIL VIA RESEND TO: [" + toEmail + "]");

        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("from", "GRILLOGIC <smertz@getgrillogic.com>");
            body.put("to", toEmail);
            body.put("subject", "Welcome to GRILLOGIC — Your Account Is Ready");
            body.put("text", textBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println(">>> Resend response status: " + response.statusCode());
            System.out.println(">>> Resend response body: " + response.body());

            if (response.statusCode() >= 300) {
                throw new RuntimeException("Resend API returned error " + response.statusCode() + ": " + response.body());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to send welcome email via Resend: " + e.getMessage(), e);
        }
    }
}