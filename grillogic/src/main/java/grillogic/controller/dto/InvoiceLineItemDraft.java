package grillogic.controller.dto;

import lombok.Data;

// One extracted line item from an invoice photo, before any human review.
// Nothing here is saved to the database yet — it's a draft the frontend displays for editing.
@Data
public class InvoiceLineItemDraft {
    private String ingredientNameGuess;
    private Double quantity;
    private String unit;
    private Double totalPrice;
}