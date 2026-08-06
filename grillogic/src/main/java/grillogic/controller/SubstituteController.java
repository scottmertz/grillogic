package grillogic.controller;

import grillogic.controller.dto.SubstituteRequest;
import grillogic.controller.dto.SubstituteResponse;
import grillogic.model.Ingredient;
import grillogic.model.IngredientSubstitute;
import grillogic.repository.IngredientRepository;
import grillogic.repository.IngredientSubstituteRepository;
import grillogic.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/substitutes")
public class SubstituteController {

    private final IngredientSubstituteRepository substituteRepository;
    private final IngredientRepository ingredientRepository;
    private final CurrentUserService currentUserService;

    @Autowired
    public SubstituteController(IngredientSubstituteRepository substituteRepository,
                                IngredientRepository ingredientRepository,
                                CurrentUserService currentUserService) {
        this.substituteRepository = substituteRepository;
        this.ingredientRepository = ingredientRepository;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public SubstituteResponse createSubstitute(@RequestBody SubstituteRequest request) {
        Long ownerId = currentUserService.getEffectiveOwnerId();

        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> new RuntimeException("Ingredient not found: " + request.getIngredientId()));
        Ingredient substitute = ingredientRepository.findById(request.getSubstituteId())
                .orElseThrow(() -> new RuntimeException("Substitute ingredient not found: " + request.getSubstituteId()));

        if (ingredient.getId().equals(substitute.getId())) {
            throw new RuntimeException("An ingredient cannot be its own substitute.");
        }

        IngredientSubstitute entry = new IngredientSubstitute();
        entry.setOwnerId(ownerId);
        entry.setIngredient(ingredient);
        entry.setSubstitute(substitute);
        entry.setNote(request.getNote());

        IngredientSubstitute saved = substituteRepository.save(entry);
        return toResponse(saved);
    }

    @GetMapping("/for/{ingredientId}")
    public List<SubstituteResponse> getSubstitutesFor(@PathVariable Long ingredientId) {
        return substituteRepository.findByIngredientId(ingredientId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    public void deleteSubstitute(@PathVariable Long id) {
        if (!substituteRepository.existsById(id)) {
            throw new RuntimeException("Substitute entry not found: " + id);
        }
        substituteRepository.deleteById(id);
    }

    private SubstituteResponse toResponse(IngredientSubstitute entry) {
        SubstituteResponse response = new SubstituteResponse();
        response.setId(entry.getId());
        response.setSubstituteId(entry.getSubstitute().getId());
        response.setSubstituteName(entry.getSubstitute().getName());
        response.setSubstituteCategory(entry.getSubstitute().getCategory());

        double pricePerUnit = entry.getSubstitute().getPurchasePrice() / entry.getSubstitute().getPurchaseAmount();
        response.setSubstitutePricePerUnit(pricePerUnit);

        response.setNote(entry.getNote());
        return response;
    }
}