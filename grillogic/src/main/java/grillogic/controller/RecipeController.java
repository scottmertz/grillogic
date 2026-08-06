package grillogic.controller;

import grillogic.controller.dto.RecipeCreateRequest;
import grillogic.controller.dto.RecipeIngredientRequest;
import grillogic.controller.dto.RecipeSummaryResponse;
import grillogic.controller.dto.RecipeResponse;
import grillogic.model.Ingredient;
import grillogic.model.Recipe;
import grillogic.model.RecipeIngredient;
import grillogic.repository.IngredientRepository;
import grillogic.repository.RecipeRepository;
import grillogic.repository.RecipeVersionRepository;
import grillogic.service.CostingService;
import grillogic.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final CostingService costingService;
    private final CurrentUserService currentUserService;
    private final RecipeVersionRepository recipeVersionRepository;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper =
            new com.fasterxml.jackson.databind.ObjectMapper();

    @Autowired
    public RecipeController(RecipeRepository recipeRepository,
                            IngredientRepository ingredientRepository,
                            CostingService costingService,
                            CurrentUserService currentUserService,
                            RecipeVersionRepository recipeVersionRepository) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.costingService = costingService;
        this.currentUserService = currentUserService;
        this.recipeVersionRepository = recipeVersionRepository;
    }

    @PostMapping
    public RecipeResponse createRecipe(@RequestBody RecipeCreateRequest request) {
        Long ownerId = currentUserService.getEffectiveOwnerId();

        Recipe recipe = new Recipe();
        recipe.setOwnerId(ownerId);
        recipe.setName(request.getName());
        recipe.setServings(request.getServings());
        recipe.setMenuPrice(request.getMenuPrice());
        recipe.setLaborCostPct(request.getLaborCostPct());
        recipe.setBatchYieldAmount(request.getBatchYieldAmount());
        recipe.setBatchYieldUnit(request.getBatchYieldUnit());
        recipe.setDedicatedLaborHours(request.getDedicatedLaborHours());
        recipe.setDedicatedLaborRate(request.getDedicatedLaborRate());
        recipe.setInstructions(request.getInstructions());

        for (RecipeIngredientRequest lineRequest : request.getIngredients()) {
            RecipeIngredient line = new RecipeIngredient();
            line.setAmount(lineRequest.getAmount());
            line.setAmountUnit(lineRequest.getAmountUnit());
            line.setRecipe(recipe);

            if (lineRequest.getIngredientId() != null) {
                Ingredient ingredient = ingredientRepository.findById(lineRequest.getIngredientId())
                        .orElseThrow(() -> new RuntimeException(
                                "Ingredient not found: " + lineRequest.getIngredientId()));
                line.setIngredient(ingredient);
            } else if (lineRequest.getSubRecipeId() != null) {
                Recipe subRecipe = recipeRepository.findById(lineRequest.getSubRecipeId())
                        .orElseThrow(() -> new RuntimeException(
                                "Sub-recipe not found: " + lineRequest.getSubRecipeId()));
                line.setSubRecipe(subRecipe);
            } else {
                throw new RuntimeException("Each recipe line must specify either an ingredientId or a subRecipeId.");
            }

            recipe.getIngredients().add(line);
        }

        Recipe saved = recipeRepository.save(recipe);
        RecipeResponse response = new RecipeResponse();
        response.setId(saved.getId());
        response.setName(saved.getName());
        response.setServings(saved.getServings());
        response.setMenuPrice(saved.getMenuPrice());
        return response;
    }

    @GetMapping
    public List<RecipeSummaryResponse> getAllRecipes() {
        Long ownerId = currentUserService.getEffectiveOwnerId();

        return recipeRepository.findAll().stream()
                .filter(r -> r.getOwnerId().equals(ownerId))
                .map(recipe -> {
                    RecipeSummaryResponse dto = new RecipeSummaryResponse();
                    dto.setId(recipe.getId());
                    dto.setName(recipe.getName());
                    dto.setServings(recipe.getServings());
                    dto.setMenuPrice(recipe.getMenuPrice());
                    dto.setTotalCost(costingService.totalRecipeCost(recipe));
                    dto.setCostPerServing(costingService.costPerServing(recipe));
                    dto.setFoodCostPct(costingService.foodCostPercent(recipe));
                    dto.setBatchYieldAmount(recipe.getBatchYieldAmount());
                    dto.setBatchYieldUnit(recipe.getBatchYieldUnit());
                    dto.setIsBatchRecipe(recipe.getBatchYieldAmount() != null && recipe.getBatchYieldUnit() != null);
                    dto.setDedicatedLaborHours(recipe.getDedicatedLaborHours());
                    dto.setDedicatedLaborRate(recipe.getDedicatedLaborRate());
                    return dto;
                })
                .collect(Collectors.toList());
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
    public RecipeResponse updateRecipe(@PathVariable Long id, @RequestBody RecipeCreateRequest request) {
        Recipe existing = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found: " + id));

        try {
            String snapshotJson = objectMapper.writeValueAsString(toSnapshotMap(existing));
            grillogic.model.RecipeVersion version = new grillogic.model.RecipeVersion();
            version.setRecipeId(existing.getId());
            version.setVersionTimestamp(java.time.LocalDateTime.now());
            version.setSnapshotJson(snapshotJson);
            recipeVersionRepository.save(version);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save recipe version snapshot: " + e.getMessage(), e);
        }

        existing.setName(request.getName());
        existing.setServings(request.getServings());
        existing.setMenuPrice(request.getMenuPrice());
        existing.setLaborCostPct(request.getLaborCostPct());
        existing.setBatchYieldAmount(request.getBatchYieldAmount());
        existing.setBatchYieldUnit(request.getBatchYieldUnit());
        existing.setDedicatedLaborHours(request.getDedicatedLaborHours());
        existing.setDedicatedLaborRate(request.getDedicatedLaborRate());
        existing.getIngredients().clear();
        existing.setInstructions(request.getInstructions());

        for (RecipeIngredientRequest lineRequest : request.getIngredients()) {
            RecipeIngredient line = new RecipeIngredient();
            line.setAmount(lineRequest.getAmount());
            line.setAmountUnit(lineRequest.getAmountUnit());
            line.setRecipe(existing);

            if (lineRequest.getIngredientId() != null) {
                Ingredient ingredient = ingredientRepository.findById(lineRequest.getIngredientId())
                        .orElseThrow(() -> new RuntimeException(
                                "Ingredient not found: " + lineRequest.getIngredientId()));
                line.setIngredient(ingredient);
            } else if (lineRequest.getSubRecipeId() != null) {
                Recipe subRecipe = recipeRepository.findById(lineRequest.getSubRecipeId())
                        .orElseThrow(() -> new RuntimeException(
                                "Sub-recipe not found: " + lineRequest.getSubRecipeId()));
                line.setSubRecipe(subRecipe);
            } else {
                throw new RuntimeException("Each recipe line must specify either an ingredientId or a subRecipeId.");
            }

            existing.getIngredients().add(line);
        }

        Recipe saved = recipeRepository.save(existing);
        RecipeResponse response = new RecipeResponse();
        response.setId(saved.getId());
        response.setName(saved.getName());
        response.setServings(saved.getServings());
        response.setMenuPrice(saved.getMenuPrice());
        return response;
    }

    private java.util.Map<String, Object> toSnapshotMap(Recipe recipe) {
        java.util.Map<String, Object> snapshot = new java.util.HashMap<>();
        snapshot.put("name", recipe.getName());
        snapshot.put("servings", recipe.getServings());
        snapshot.put("menuPrice", recipe.getMenuPrice());
        snapshot.put("laborCostPct", recipe.getLaborCostPct());
        snapshot.put("batchYieldAmount", recipe.getBatchYieldAmount());
        snapshot.put("batchYieldUnit", recipe.getBatchYieldUnit());
        snapshot.put("dedicatedLaborHours", recipe.getDedicatedLaborHours());
        snapshot.put("dedicatedLaborRate", recipe.getDedicatedLaborRate());
        snapshot.put("instructions", recipe.getInstructions());

        java.util.List<java.util.Map<String, Object>> lines = recipe.getIngredients().stream()
                .map(line -> {
                    java.util.Map<String, Object> lineMap = new java.util.HashMap<>();
                    if (line.getIngredient() != null) {
                        lineMap.put("type", "ingredient");
                        lineMap.put("name", line.getIngredient().getName());
                    } else {
                        lineMap.put("type", "subrecipe");
                        lineMap.put("name", line.getSubRecipe().getName());
                    }
                    lineMap.put("amount", line.getAmount());
                    lineMap.put("unit", line.getAmountUnit());
                    return lineMap;
                })
                .collect(java.util.stream.Collectors.toList());
        snapshot.put("ingredients", lines);

        return snapshot;
    }

    @DeleteMapping("/{id}")
    public void deleteRecipe(@PathVariable Long id) {
        if (!recipeRepository.existsById(id)) {
            throw new RuntimeException("Recipe not found: " + id);
        }
        recipeRepository.deleteById(id);
    }

    @GetMapping("/{id}/detail")
    public java.util.Map<String, Object> getRecipeDetail(@PathVariable Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found: " + id));

        java.util.List<java.util.Map<String, Object>> lines = recipe.getIngredients().stream()
                .map(line -> {
                    java.util.Map<String, Object> lineMap = new java.util.HashMap<>();
                    if (line.getIngredient() != null) {
                        lineMap.put("name", line.getIngredient().getName());
                        lineMap.put("type", "Ingredient");
                    } else {
                        lineMap.put("name", line.getSubRecipe().getName());
                        lineMap.put("type", "Sub-Recipe");
                    }
                    lineMap.put("amount", line.getAmount());
                    lineMap.put("unit", line.getAmountUnit());
                    lineMap.put("cost", costingService.costOfLine(line));
                    return lineMap;
                })
                .collect(java.util.stream.Collectors.toList());

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", recipe.getId());
        result.put("name", recipe.getName());
        result.put("servings", recipe.getServings());
        result.put("menuPrice", recipe.getMenuPrice());
        result.put("laborCostPct", recipe.getLaborCostPct());
        result.put("batchYieldAmount", recipe.getBatchYieldAmount());
        result.put("batchYieldUnit", recipe.getBatchYieldUnit());
        result.put("dedicatedLaborHours", recipe.getDedicatedLaborHours());
        result.put("dedicatedLaborRate", recipe.getDedicatedLaborRate());
        result.put("lines", lines);
        result.put("totalCost", costingService.totalRecipeCost(recipe));
        result.put("costPerServing", costingService.costPerServing(recipe));
        result.put("foodCostPct", costingService.foodCostPercent(recipe));

        return result;
    }
}