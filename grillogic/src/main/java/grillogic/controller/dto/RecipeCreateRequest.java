package grillogic.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecipeCreateRequest {
    private String name;
    private Integer servings;
    private Double menuPrice;
    private Double laborCostPct;
    private List<RecipeIngredientRequest> ingredients;
    private Double batchYieldAmount;
    private grillogic.model.Unit batchYieldUnit;
    private Double dedicatedLaborHours;
    private Double dedicatedLaborRate;
    private String instructions;
}