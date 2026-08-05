package grillogic.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

// Talks directly to Anthropic's Messages API using the JDK's built-in HTTP client —
// no extra Maven dependency needed, since java.net.http ships with Java 11+.
@Service
public class ClaudeVisionService {

    // Left blank by default so the app can still start without this configured —
    // only the invoice capture feature itself fails until you add the real key.
    @Value("${anthropic.api.key:}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String MODEL = "claude-sonnet-5";

    private static final String PROMPT = """
            You are reading a photo of a restaurant vendor invoice or receipt. Extract the data as JSON only \
            — no markdown formatting, no explanation, just the raw JSON object. Use this exact shape:
            {
              "vendorNameGuess": "string, or null if no vendor name is visible",
              "lineItems": [
                {
                  "ingredientNameGuess": "string",
                  "quantity": number,
                  "unit": "one of: G, KG, OZ, LB, ML, L, FL_OZ, CUP, TSP, TBSP, EACH — pick the closest match, default to EACH if unclear",
                  "totalPrice": number
                }
              ]
            }
            If a field is illegible, make your best guess rather than skipping the line item. \
            Output only the JSON object and nothing else.
            """;

    public JsonNode extractInvoiceData(byte[] imageBytes, String mediaType) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException(
                    "Anthropic API key not configured. Add anthropic.api.key=YOUR_KEY to application-local.properties.");
        }

        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String requestBody = buildRequestBody(base64Image, mediaType);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.anthropic.com/v1/messages"))
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Claude API error (status " + response.statusCode() + "): " + response.body());
            }

            JsonNode responseJson = objectMapper.readTree(response.body());
            String textContent = responseJson.path("content").get(0).path("text").asText();

            // Claude occasionally wraps JSON in markdown fences even when told not to —
            // strip them defensively rather than trust the instruction alone.
            String cleaned = textContent.replace("```json", "").replace("```", "").trim();

            return objectMapper.readTree(cleaned);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract invoice data: " + e.getMessage(), e);
        }
    }

    private String buildRequestBody(String base64Image, String mediaType) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", MODEL);
            root.put("max_tokens", 2000);

            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode message = objectMapper.createObjectNode();
            message.put("role", "user");

            ArrayNode content = objectMapper.createArrayNode();

            ObjectNode imageBlock = objectMapper.createObjectNode();
            imageBlock.put("type", "image");
            ObjectNode source = objectMapper.createObjectNode();
            source.put("type", "base64");
            source.put("media_type", mediaType);
            source.put("data", base64Image);
            imageBlock.set("source", source);
            content.add(imageBlock);

            ObjectNode textBlock = objectMapper.createObjectNode();
            textBlock.put("type", "text");
            textBlock.put("text", PROMPT);
            content.add(textBlock);

            message.set("content", content);
            messages.add(message);
            root.set("messages", messages);

            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build Claude API request: " + e.getMessage(), e);
        }
    }
}