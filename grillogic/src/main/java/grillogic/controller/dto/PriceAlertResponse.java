package grillogic.controller.dto;

import lombok.Data;

// One flagged vendor price change — a quote whose price moved enough since the
// last update to be worth the client's attention.
@Data
public class PriceAlertResponse {
    private Long ingredientId;
    private String ingredientName;
    private String vendorName;
    private Double previousPrice;
    private Double currentPrice;
    private Double percentChange;
    private String lastUpdated;
}