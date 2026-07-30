package grillogic.controller.dto;

import lombok.Data;

@Data
public class RecipeSummaryResponse {
    private Long id;
    private String name;
    private Integer servings;
    private Double menuPrice;
    private Double totalCost;
    private Double costPerServing;
    private Double foodCostPct;
    private Double batchYieldAmount;
    private grillogic.model.Unit batchYieldUnit;
    private Boolean isBatchRecipe; // true if batchYieldAmount/Unit are set
    private Double dedicatedLaborHours;
    private Double dedicatedLaborRate;
}