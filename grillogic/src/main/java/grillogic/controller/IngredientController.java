package grillogic.controller;

import grillogic.model.Ingredient;
import grillogic.repository.IngredientRepository;
import grillogic.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {

    private final IngredientRepository ingredientRepository;
    private final CurrentUserService currentUserService;

    @Autowired
    public IngredientController(IngredientRepository ingredientRepository,
                                CurrentUserService currentUserService) {
        this.ingredientRepository = ingredientRepository;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public Ingredient createIngredient(@RequestBody Ingredient ingredient) {
        Long ownerId = currentUserService.getEffectiveOwnerId();
        ingredient.setOwnerId(ownerId);
        return ingredientRepository.save(ingredient);
    }

    @GetMapping
    public List<Ingredient> getAllIngredients() {
        Long ownerId = currentUserService.getEffectiveOwnerId();
        return ingredientRepository.findAll().stream()
                .filter(i -> i.getOwnerId().equals(ownerId))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Ingredient getIngredient(@PathVariable Long id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingredient not found: " + id));
    }

    @PutMapping("/{id}")
    public Ingredient updateIngredient(@PathVariable Long id, @RequestBody Ingredient updated) {
        Ingredient existing = ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingredient not found: " + id));

        existing.setName(updated.getName());
        existing.setPurchaseUnit(updated.getPurchaseUnit());
        existing.setPurchasePrice(updated.getPurchasePrice());
        existing.setPurchaseAmount(updated.getPurchaseAmount());
        existing.setCategory(updated.getCategory());
        existing.setYieldPct(updated.getYieldPct());

        return ingredientRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    public void deleteIngredient(@PathVariable Long id) {
        if (!ingredientRepository.existsById(id)) {
            throw new RuntimeException("Ingredient not found: " + id);
        }
        ingredientRepository.deleteById(id);
    }
}