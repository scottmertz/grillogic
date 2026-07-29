package grillogic.service;

import grillogic.model.Ingredient;
import grillogic.model.Recipe;
import grillogic.model.RecipeIngredient;
import grillogic.model.Unit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CostingService {

    private final UnitConverter unitConverter;

    // Constructor injection — Spring sees this constructor and automatically
    // hands us a UnitConverter instance. This is the "dependency injection"
    // I mentioned back when we built UnitConverter — here it is in action.
    @Autowired
    public CostingService(UnitConverter unitConverter) {
        this.unitConverter = unitConverter;
    }

    /**
     * Cost of a single recipe-ingredient line (e.g. "7 oz of ketchup" -> $0.44)
     * Accounts for unit conversion and yield loss.
     */
    public double costOfLine(RecipeIngredient line) {
        Ingredient ingredient = line.getIngredient();

        // Cost per purchase unit (e.g. $42.00 / 42 lb = $1.00 per lb)
        double costPerPurchaseUnit = ingredient.getPurchasePrice() / ingredient.getPurchaseAmount();

        // Convert the recipe's amount into the same unit as the purchase unit
        // e.g. recipe calls for 7 OZ, ingredient is purchased in LB -> convert 7 OZ to LB
        double amountInPurchaseUnit = unitConverter.convert(
                line.getAmount(),
                line.getAmountUnit(),
                ingredient.getPurchaseUnit()
        );

        double rawCost = amountInPurchaseUnit * costPerPurchaseUnit;

        // Apply yield loss: if yieldPct is 0.85 (15% loss), you needed MORE raw
        // product to get this much usable product, so cost goes UP.
        // Null or 1.0 yieldPct means no loss adjustment needed.
        Double yieldPct = ingredient.getYieldPct();
        if (yieldPct != null && yieldPct > 0 && yieldPct < 1.0) {
            rawCost = rawCost / yieldPct;
        }

        return rawCost;
    }

    /**
     * Total raw cost of the entire recipe (sum of every ingredient line).
     */
    public double totalRecipeCost(Recipe recipe) {
        double total = 0.0;
        for (RecipeIngredient line : recipe.getIngredients()) {
            total += costOfLine(line);
        }
        return total;
    }

    /**
     * Cost per single portion/serving.
     */
    public double costPerServing(Recipe recipe) {
        double total = totalRecipeCost(recipe);
        int servings = recipe.getServings();
        if (servings <= 0) {
            throw new IllegalArgumentException("Recipe servings must be greater than 0");
        }
        return total / servings;
    }

    /**
     * Food cost % = cost per serving / menu price.
     * Returns null if there's no menu price set (not every recipe is a sellable dish —
     * some are sub-recipes/batch items like your Treehouse sauces).
     */
    public Double foodCostPercent(Recipe recipe) {
        if (recipe.getMenuPrice() == null || recipe.getMenuPrice() <= 0) {
            return null;
        }
        double perServing = costPerServing(recipe);
        return perServing / recipe.getMenuPrice();
    }
}