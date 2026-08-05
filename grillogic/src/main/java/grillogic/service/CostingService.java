package grillogic.service;

import grillogic.model.Ingredient;
import grillogic.model.IngredientVendorPrice;
import grillogic.model.Recipe;
import grillogic.model.RecipeIngredient;
import grillogic.model.Unit;
import grillogic.repository.IngredientVendorPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CostingService {

    private final UnitConverter unitConverter;
    private final IngredientVendorPriceRepository ingredientVendorPriceRepository;

    // Constructor injection — Spring sees this constructor and automatically
    // hands us a UnitConverter and IngredientVendorPriceRepository instance.
    @Autowired
    public CostingService(UnitConverter unitConverter,
                          IngredientVendorPriceRepository ingredientVendorPriceRepository) {
        this.unitConverter = unitConverter;
        this.ingredientVendorPriceRepository = ingredientVendorPriceRepository;
    }

    /**
     * Cost of a single recipe-ingredient line (e.g. "7 oz of ketchup" -> $0.44)
     * Accounts for unit conversion and yield loss.
     */
    public double costOfLine(RecipeIngredient line) {
        if (line.getIngredient() != null) {
            return costFromIngredient(line);
        } else if (line.getSubRecipe() != null) {
            return costFromSubRecipe(line);
        } else {
            throw new IllegalStateException("RecipeIngredient line has neither an ingredient nor a sub-recipe set.");
        }
    }

    private double costFromIngredient(RecipeIngredient line) {
        Ingredient ingredient = line.getIngredient();

        // Default to the ingredient's own flat purchase data.
        double purchasePrice = ingredient.getPurchasePrice();
        double purchaseAmount = ingredient.getPurchaseAmount();
        Unit purchaseUnit = ingredient.getPurchaseUnit();

        // If a preferred vendor quote exists for this ingredient, that quote wins —
        // this is what makes costing "vendor-aware" instead of a single flat price.
        Optional<IngredientVendorPrice> preferred =
                ingredientVendorPriceRepository.findByIngredientIdAndIsPreferredTrue(ingredient.getId());
        if (preferred.isPresent()) {
            IngredientVendorPrice ivp = preferred.get();
            purchasePrice = ivp.getPurchasePrice();
            purchaseAmount = ivp.getPurchaseAmount();
            purchaseUnit = ivp.getPurchaseUnit();
        }

        double costPerPurchaseUnit = purchasePrice / purchaseAmount;
        double amountInPurchaseUnit = unitConverter.convert(line.getAmount(), line.getAmountUnit(), purchaseUnit);
        double rawCost = amountInPurchaseUnit * costPerPurchaseUnit;

        Double yieldPct = ingredient.getYieldPct();
        if (yieldPct != null && yieldPct > 0 && yieldPct < 1.0) {
            rawCost = rawCost / yieldPct;
        }
        return rawCost;
    }

    private double costFromSubRecipe(RecipeIngredient line) {
        Recipe subRecipe = line.getSubRecipe();
        if (subRecipe.getBatchYieldAmount() == null || subRecipe.getBatchYieldUnit() == null) {
            throw new IllegalStateException(
                    "Recipe '" + subRecipe.getName() + "' has no batch yield set, so it can't be used as a sub-recipe.");
        }

        double subRecipeTotalCost = totalRecipeCost(subRecipe); // recursive call
        double costPerBatchUnit = subRecipeTotalCost / subRecipe.getBatchYieldAmount();

        double amountInBatchUnit = unitConverter.convert(line.getAmount(), line.getAmountUnit(), subRecipe.getBatchYieldUnit());
        return amountInBatchUnit * costPerBatchUnit;
    }

    /**
     * Total raw cost of the entire recipe (sum of every ingredient line).
     */
    public double totalRecipeCost(Recipe recipe) {
        double total = 0.0;
        for (RecipeIngredient line : recipe.getIngredients()) {
            total += costOfLine(line);
        }

        // Optional dedicated labor cost (e.g. a paid pit boss), only added
        // if the operator has actually set both hours and a rate.
        if (recipe.getDedicatedLaborHours() != null && recipe.getDedicatedLaborRate() != null) {
            total += recipe.getDedicatedLaborHours() * recipe.getDedicatedLaborRate();
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