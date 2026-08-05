package grillogic.controller;

import com.fasterxml.jackson.databind.JsonNode;
import grillogic.controller.dto.InvoiceExtractionResponse;
import grillogic.controller.dto.InvoiceLineItemDraft;
import grillogic.service.ClaudeVisionService;
import grillogic.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

// This endpoint only reads an uploaded photo and returns a draft — it never writes
// to the database. Saving happens later (Step 15b), only after a human reviews
// and confirms each line item.
@RestController
@RequestMapping("/api/invoice-capture")
public class InvoiceCaptureController {

    private final ClaudeVisionService claudeVisionService;
    private final CurrentUserService currentUserService;

    @Autowired
    public InvoiceCaptureController(ClaudeVisionService claudeVisionService,
                                    CurrentUserService currentUserService) {
        this.claudeVisionService = claudeVisionService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/extract")
    public InvoiceExtractionResponse extractInvoice(@RequestParam("image") MultipartFile image) throws Exception {
        currentUserService.getCurrentUser(); // confirms the caller is logged in

        String mediaType = image.getContentType();
        if (mediaType == null || !(mediaType.equals("image/jpeg") || mediaType.equals("image/png")
                || mediaType.equals("image/webp") || mediaType.equals("image/gif"))) {
            throw new RuntimeException("Unsupported image type. Please upload a JPEG, PNG, WEBP, or GIF.");
        }

        JsonNode extracted = claudeVisionService.extractInvoiceData(image.getBytes(), mediaType);

        InvoiceExtractionResponse response = new InvoiceExtractionResponse();
        response.setVendorNameGuess(
                extracted.path("vendorNameGuess").isNull() ? null : extracted.path("vendorNameGuess").asText(null));

        List<InvoiceLineItemDraft> lineItems = new ArrayList<>();
        for (JsonNode item : extracted.path("lineItems")) {
            InvoiceLineItemDraft draft = new InvoiceLineItemDraft();
            draft.setIngredientNameGuess(item.path("ingredientNameGuess").asText(null));
            draft.setQuantity(item.path("quantity").isMissingNode() ? null : item.path("quantity").asDouble());
            draft.setUnit(item.path("unit").asText(null));
            draft.setTotalPrice(item.path("totalPrice").isMissingNode() ? null : item.path("totalPrice").asDouble());
            lineItems.add(draft);
        }
        response.setLineItems(lineItems);

        return response;
    }
}