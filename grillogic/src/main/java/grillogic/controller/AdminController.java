package grillogic.controller;

import grillogic.controller.dto.AdminClientDetailResponse;
import grillogic.controller.dto.AdminClientRow;
import grillogic.controller.dto.RecipeSummaryResponse;
import grillogic.model.Ingredient;
import grillogic.model.Recipe;
import grillogic.model.User;
import grillogic.repository.IngredientRepository;
import grillogic.repository.RecipeRepository;
import grillogic.repository.UserRepository;
import grillogic.service.CostingService;
import grillogic.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final CostingService costingService;
    private final CurrentUserService currentUserService;

    @Autowired
    public AdminController(UserRepository userRepository,
                           RecipeRepository recipeRepository,
                           IngredientRepository ingredientRepository,
                           CostingService costingService,
                           CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.costingService = costingService;
        this.currentUserService = currentUserService;
    }

    private void requireAdmin() {
        User currentUser = currentUserService.getCurrentUser();
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("Access denied: admin only");
        }
    }

    @GetMapping("/clients")
    public List<AdminClientRow> getAllClients() {
        requireAdmin();

        return userRepository.findAll().stream()
                .map(user -> {
                    AdminClientRow row = new AdminClientRow();
                    row.setUserId(user.getId());
                    row.setEmail(user.getEmail());
                    row.setBusinessName(user.getBusinessName());
                    row.setTier(user.getTier());
                    row.setSubscriptionActive(user.getSubscriptionActive());

                    long recipeCount = recipeRepository.findAll().stream()
                            .filter(r -> r.getOwnerId().equals(user.getId()))
                            .count();
                    long ingredientCount = ingredientRepository.findAll().stream()
                            .filter(i -> i.getOwnerId().equals(user.getId()))
                            .count();

                    row.setRecipeCount((int) recipeCount);
                    row.setIngredientCount((int) ingredientCount);
                    return row;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/clients/{id}")
    public AdminClientDetailResponse getClientDetail(@PathVariable Long id) {
        requireAdmin();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found: " + id));

        List<Recipe> recipes = recipeRepository.findAll().stream()
                .filter(r -> r.getOwnerId().equals(user.getId()))
                .collect(Collectors.toList());

        List<RecipeSummaryResponse> recipeSummaries = recipes.stream()
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

        List<Ingredient> ingredients = ingredientRepository.findAll().stream()
                .filter(i -> i.getOwnerId().equals(user.getId()))
                .collect(Collectors.toList());

        AdminClientDetailResponse response = new AdminClientDetailResponse();
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setBusinessName(user.getBusinessName());
        response.setTier(user.getTier());
        response.setSubscriptionActive(user.getSubscriptionActive());
        response.setRecipes(recipeSummaries);
        response.setIngredients(ingredients);

        return response;
    }

    @PutMapping("/clients/{id}")
    public String updateClient(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        requireAdmin();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found: " + id));

        if (updates.containsKey("businessName")) {
            user.setBusinessName((String) updates.get("businessName"));
        }
        if (updates.containsKey("tier")) {
            user.setTier((String) updates.get("tier"));
        }
        if (updates.containsKey("subscriptionActive")) {
            user.setSubscriptionActive((Boolean) updates.get("subscriptionActive"));
        }

        userRepository.save(user);
        return "Updated";
    }
}