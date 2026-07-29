package grillogic.controller;

import grillogic.controller.dto.RecipeCreateRequest;
import grillogic.controller.dto.RecipeIngredientRequest;
import grillogic.model.Ingredient;
import grillogic.model.Recipe;
import grillogic.model.RecipeIngredient;
import grillogic.repository.IngredientRepository;
import grillogic.repository.RecipeRepository;
import grillogic.service.CostingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final CostingService costingService;

    @Autowired
    public RecipeController(RecipeRepository recipeRepository,
                            IngredientRepository ingredientRepository,
                            CostingService costingService) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.costingService = costingService;
    }

    @PostMapping
    public Recipe createRecipe(@RequestBody RecipeCreateRequest request) {
        Recipe recipe = new Recipe();
        recipe.setName(request.getName());
        recipe.setServings(request.getServings());
        recipe.setMenuPrice(request.getMenuPrice());
        recipe.setLaborCostPct(request.getLaborCostPct());

        for (RecipeIngredientRequest lineRequest : request.getIngredients()) {
            Ingredient ingredient = ingredientRepository.findById(lineRequest.getIngredientId())
                    .orElseThrow(() -> new RuntimeException(
                            "Ingredient not found: " + lineRequest.getIngredientId()));

            RecipeIngredient line = new RecipeIngredient();
            line.setIngredient(ingredient);
            line.setAmount(lineRequest.getAmount());
            line.setAmountUnit(lineRequest.getAmountUnit());
            line.setRecipe(recipe); // link back to the parent recipe

            recipe.getIngredients().add(line);
        }

        return recipeRepository.save(recipe);
    }

    @GetMapping("/{id}/cost")
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

    @PutMapping("/{id}")
    public Recipe updateRecipe(@PathVariable Long id, @RequestBody RecipeCreateRequest request) {
        Recipe existing = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found: " + id));

        existing.setName(request.getName());
        existing.setServings(request.getServings());
        existing.setMenuPrice(request.getMenuPrice());
        existing.setLaborCostPct(request.getLaborCostPct());

        // Clear the old ingredient lines — orphanRemoval=true (set on Recipe.ingredients)
        // means Hibernate will actually DELETE the old rows, not just unlink them.
        existing.getIngredients().clear();

        for (RecipeIngredientRequest lineRequest : request.getIngredients()) {
            Ingredient ingredient = ingredientRepository.findById(lineRequest.getIngredientId())
                    .orElseThrow(() -> new RuntimeException(
                            "Ingredient not found: " + lineRequest.getIngredientId()));

            RecipeIngredient line = new RecipeIngredient();
            line.setIngredient(ingredient);
            line.setAmount(lineRequest.getAmount());
            line.setAmountUnit(lineRequest.getAmountUnit());
            line.setRecipe(existing);

            existing.getIngredients().add(line);
        }

        return recipeRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    public void deleteRecipe(@PathVariable Long id) {
        if (!recipeRepository.existsById(id)) {
            throw new RuntimeException("Recipe not found: " + id);
        }
        recipeRepository.deleteById(id);
    }
}