package grillogic.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class InvoiceExtractionResponse {
    private String vendorNameGuess;
    private List<InvoiceLineItemDraft> lineItems;
}