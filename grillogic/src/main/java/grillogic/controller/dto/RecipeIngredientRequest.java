package grillogic.controller.dto;

import grillogic.model.Unit;
import lombok.Data;

@Data
public class RecipeIngredientRequest {
    private Long ingredientId;   // which existing Ingredient to link to
    private Long subRecipeId;    // set this instead if the line is a sub-recipe
    private Double amount;
    private Unit amountUnit;
}