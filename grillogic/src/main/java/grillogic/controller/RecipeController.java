package grillogic.controller;

import grillogic.model.Recipe;
import grillogic.repository.RecipeRepository;
import grillogic.service.CostingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecipeController {

    private final RecipeRepository recipeRepository;
    private final CostingService costingService;

    @Autowired
    public RecipeController(RecipeRepository recipeRepository, CostingService costingService) {
        this.recipeRepository = recipeRepository;
        this.costingService = costingService;
    }

    @GetMapping("/api/recipes/{id}/cost")
    public String getRecipeCost(@PathVariable Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found: " + id));

        double totalCost = costingService.totalRecipeCost(recipe);
        double perServing = costingService.costPerServing(recipe);
        Double foodCostPct = costingService.foodCostPercent(recipe);

        return String.format(
                "Recipe: %s | Total Cost: $%.2f | Per Serving: $%.2f | Food Cost %%: %s",
                recipe.getName(),
                totalCost,
                perServing,
                foodCostPct != null ? String.format("%.1f%%", foodCostPct * 100) : "N/A"
        );
    }
}