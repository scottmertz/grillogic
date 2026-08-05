package grillogic.controller;

import grillogic.controller.dto.WasteEntryRequest;
import grillogic.controller.dto.WasteEntryResponse;
import grillogic.model.Ingredient;
import grillogic.model.Recipe;
import grillogic.model.WasteEntry;
import grillogic.repository.IngredientRepository;
import grillogic.repository.RecipeRepository;
import grillogic.repository.WasteEntryRepository;
import grillogic.service.CostingService;
import grillogic.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/waste")
public class WasteController {

    private final WasteEntryRepository wasteEntryRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final CostingService costingService;
    private final CurrentUserService currentUserService;

    @Autowired
    public WasteController(WasteEntryRepository wasteEntryRepository,
                           IngredientRepository ingredientRepository,
                           RecipeRepository recipeRepository,
                           CostingService costingService,
                           CurrentUserService currentUserService) {
        this.wasteEntryRepository = wasteEntryRepository;
        this.ingredientRepository = ingredientRepository;
        this.recipeRepository = recipeRepository;
        this.costingService = costingService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public WasteEntryResponse createWasteEntry(@RequestBody WasteEntryRequest request) {
        Long ownerId = currentUserService.getCurrentUser().getId();

        WasteEntry entry = new WasteEntry();
        entry.setOwnerId(ownerId);
        entry.setAmount(request.getAmount());
        entry.setAmountUnit(request.getAmountUnit());
        entry.setWasteDate(request.getWasteDate() != null ? request.getWasteDate() : LocalDate.now());
        entry.setReason(request.getReason());

        if (request.getIngredientId() != null) {
            Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                    .orElseThrow(() -> new RuntimeException("Ingredient not found: " + request.getIngredientId()));
            entry.setIngredient(ingredient);
        } else if (request.getSubRecipeId() != null) {
            Recipe subRecipe = recipeRepository.findById(request.getSubRecipeId())
                    .orElseThrow(() -> new RuntimeException("Sub-recipe not found: " + request.getSubRecipeId()));
            entry.setSubRecipe(subRecipe);
        } else {
            throw new RuntimeException("Each waste entry must specify either an ingredientId or a subRecipeId.");
        }

        WasteEntry saved = wasteEntryRepository.save(entry);
        return toResponse(saved);
    }

    @GetMapping
    public List<WasteEntryResponse> getWasteEntries(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        Long ownerId = currentUserService.getCurrentUser().getId();

        return wasteEntryRepository.findAll().stream()
                .filter(w -> w.getOwnerId().equals(ownerId))
                .filter(w -> startDate == null || !w.getWasteDate().isBefore(startDate))
                .filter(w -> endDate == null || !w.getWasteDate().isAfter(endDate))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    public void deleteWasteEntry(@PathVariable Long id) {
        if (!wasteEntryRepository.existsById(id)) {
            throw new RuntimeException("Waste entry not found: " + id);
        }
        wasteEntryRepository.deleteById(id);
    }

    private WasteEntryResponse toResponse(WasteEntry entry) {
        WasteEntryResponse response = new WasteEntryResponse();
        response.setId(entry.getId());
        response.setAmount(entry.getAmount());
        response.setUnit(entry.getAmountUnit().toString());
        response.setWasteDate(entry.getWasteDate());
        response.setReason(entry.getReason());

        double cost;
        if (entry.getIngredient() != null) {
            response.setItemName(entry.getIngredient().getName());
            response.setItemType("Ingredient");
            cost = costOfWasteLine(entry.getIngredient(), entry.getAmount(), entry.getAmountUnit());
        } else {
            response.setItemName(entry.getSubRecipe().getName());
            response.setItemType("Sub-Recipe");
            cost = costOfSubRecipeWaste(entry.getSubRecipe(), entry.getAmount(), entry.getAmountUnit());
        }
        response.setCost(cost);

        return response;
    }

    private double costOfWasteLine(Ingredient ingredient, Double amount, grillogic.model.Unit unit) {
        // Build a temporary RecipeIngredient just to reuse CostingService's existing math —
        // avoids duplicating the unit-conversion + yield-adjustment logic a third time.
        grillogic.model.RecipeIngredient tempLine = new grillogic.model.RecipeIngredient();
        tempLine.setIngredient(ingredient);
        tempLine.setAmount(amount);
        tempLine.setAmountUnit(unit);
        return costingService.costOfLine(tempLine);
    }

    private double costOfSubRecipeWaste(Recipe subRecipe, Double amount, grillogic.model.Unit unit) {
        grillogic.model.RecipeIngredient tempLine = new grillogic.model.RecipeIngredient();
        tempLine.setSubRecipe(subRecipe);
        tempLine.setAmount(amount);
        tempLine.setAmountUnit(unit);
        return costingService.costOfLine(tempLine);
    }
}