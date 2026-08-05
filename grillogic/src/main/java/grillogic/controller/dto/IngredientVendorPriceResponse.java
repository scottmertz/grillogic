package grillogic.controller.dto;

import grillogic.model.Unit;
import lombok.Data;

// What we send back to the browser — flattened so the frontend doesn't have to dig
// into a nested Vendor object just to show the vendor's name.
@Data
public class IngredientVendorPriceResponse {
    private Long id;
    private Long vendorId;
    private String vendorName;
    private Double purchasePrice;
    private Double purchaseAmount;
    private Unit purchaseUnit;
    private Boolean isPreferred;
    private Double previousPrice;
    private String lastUpdated;
}