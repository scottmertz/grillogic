package grillogic.controller.dto;

import grillogic.model.Unit;
import lombok.Data;

// What the browser sends us when adding or updating a vendor's price quote for an ingredient.
@Data
public class IngredientVendorPriceRequest {
    private Long vendorId;
    private Double purchasePrice;
    private Double purchaseAmount;
    private Unit purchaseUnit;
}