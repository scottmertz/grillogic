package grillogic.controller.dto;

import grillogic.model.Unit;
import lombok.Data;

@Data
public class RecipeIngredientRequest {
    private Long ingredientId;   // which existing Ingredient to link to
    private Double amount;
    private Unit amountUnit;
}