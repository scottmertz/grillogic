package grillogic.controller;

import grillogic.controller.dto.IngredientVendorPriceRequest;
import grillogic.controller.dto.IngredientVendorPriceResponse;
import grillogic.model.Ingredient;
import grillogic.model.IngredientVendorPrice;
import grillogic.model.Vendor;
import grillogic.repository.IngredientRepository;
import grillogic.repository.IngredientVendorPriceRepository;
import grillogic.repository.VendorRepository;
import grillogic.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ingredients/{ingredientId}/vendor-prices")
public class IngredientVendorPriceController {

    private final IngredientVendorPriceRepository vendorPriceRepository;
    private final IngredientRepository ingredientRepository;
    private final VendorRepository vendorRepository;
    private final CurrentUserService currentUserService;

    @Autowired
    public IngredientVendorPriceController(IngredientVendorPriceRepository vendorPriceRepository,
                                           IngredientRepository ingredientRepository,
                                           VendorRepository vendorRepository,
                                           CurrentUserService currentUserService) {
        this.vendorPriceRepository = vendorPriceRepository;
        this.ingredientRepository = ingredientRepository;
        this.vendorRepository = vendorRepository;
        this.currentUserService = currentUserService;
    }

    // Confirms the ingredient exists AND belongs to whoever is currently logged in —
    // same ownership check pattern used everywhere else in the app.
    private Ingredient requireOwnedIngredient(Long ingredientId) {
        Long ownerId = currentUserService.getCurrentUser().getId();
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new RuntimeException("Ingredient not found: " + ingredientId));
        if (!ingredient.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Access denied: not your ingredient");
        }
        return ingredient;
    }

    private IngredientVendorPriceResponse toResponse(IngredientVendorPrice ivp) {
        IngredientVendorPriceResponse dto = new IngredientVendorPriceResponse();
        dto.setId(ivp.getId());
        dto.setVendorId(ivp.getVendor().getId());
        dto.setVendorName(ivp.getVendor().getName());
        dto.setPurchasePrice(ivp.getPurchasePrice());
        dto.setPurchaseAmount(ivp.getPurchaseAmount());
        dto.setPurchaseUnit(ivp.getPurchaseUnit());
        dto.setIsPreferred(ivp.getIsPreferred());
        dto.setPreviousPrice(ivp.getPreviousPrice());
        dto.setLastUpdated(ivp.getLastUpdated() != null
                ? ivp.getLastUpdated().format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                : null);
        return dto;
    }

    @GetMapping
    public List<IngredientVendorPriceResponse> getVendorPrices(@PathVariable Long ingredientId) {
        requireOwnedIngredient(ingredientId);
        return vendorPriceRepository.findByIngredientId(ingredientId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @PostMapping
    public IngredientVendorPriceResponse addVendorPrice(@PathVariable Long ingredientId,
                                                        @RequestBody IngredientVendorPriceRequest request) {
        Ingredient ingredient = requireOwnedIngredient(ingredientId);

        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found: " + request.getVendorId()));

        IngredientVendorPrice ivp = new IngredientVendorPrice();
        ivp.setIngredient(ingredient);
        ivp.setVendor(vendor);
        ivp.setPurchasePrice(request.getPurchasePrice());
        ivp.setPurchaseAmount(request.getPurchaseAmount());
        ivp.setPurchaseUnit(request.getPurchaseUnit());
        ivp.setIsPreferred(false);
        ivp.setLastUpdated(LocalDateTime.now());

        return toResponse(vendorPriceRepository.save(ivp));
    }

    @PutMapping("/{vendorPriceId}")
    public IngredientVendorPriceResponse updateVendorPrice(@PathVariable Long ingredientId,
                                                           @PathVariable Long vendorPriceId,
                                                           @RequestBody IngredientVendorPriceRequest request) {
        requireOwnedIngredient(ingredientId);

        IngredientVendorPrice existing = vendorPriceRepository.findById(vendorPriceId)
                .orElseThrow(() -> new RuntimeException("Vendor price not found: " + vendorPriceId));

        if (!existing.getPurchasePrice().equals(request.getPurchasePrice())) {
            existing.setPreviousPrice(existing.getPurchasePrice());
        }

        existing.setPurchasePrice(request.getPurchasePrice());
        existing.setPurchaseAmount(request.getPurchaseAmount());
        existing.setPurchaseUnit(request.getPurchaseUnit());
        existing.setLastUpdated(LocalDateTime.now());

        return toResponse(vendorPriceRepository.save(existing));
    }

    @PutMapping("/{vendorPriceId}/prefer")
    public IngredientVendorPriceResponse setPreferredVendorPrice(@PathVariable Long ingredientId,
                                                                 @PathVariable Long vendorPriceId) {
        requireOwnedIngredient(ingredientId);

        List<IngredientVendorPrice> allForIngredient = vendorPriceRepository.findByIngredientId(ingredientId);
        IngredientVendorPrice target = null;

        for (IngredientVendorPrice ivp : allForIngredient) {
            boolean isTarget = ivp.getId().equals(vendorPriceId);
            ivp.setIsPreferred(isTarget);
            if (isTarget) {
                target = ivp;
            }
        }

        if (target == null) {
            throw new RuntimeException("Vendor price not found: " + vendorPriceId);
        }

        vendorPriceRepository.saveAll(allForIngredient);
        return toResponse(target);
    }

    @DeleteMapping("/{vendorPriceId}")
    public void deleteVendorPrice(@PathVariable Long ingredientId, @PathVariable Long vendorPriceId) {
        requireOwnedIngredient(ingredientId);
        if (!vendorPriceRepository.existsById(vendorPriceId)) {
            throw new RuntimeException("Vendor price not found: " + vendorPriceId);
        }
        vendorPriceRepository.deleteById(vendorPriceId);
    }
}