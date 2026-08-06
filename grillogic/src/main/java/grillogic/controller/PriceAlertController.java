package grillogic.controller;

import grillogic.controller.dto.PriceAlertResponse;
import grillogic.model.Ingredient;
import grillogic.model.IngredientVendorPrice;
import grillogic.repository.IngredientRepository;
import grillogic.repository.IngredientVendorPriceRepository;
import grillogic.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/price-alerts")
public class PriceAlertController {

    private static final double ALERT_THRESHOLD_PCT = 10.0;

    private final IngredientVendorPriceRepository vendorPriceRepository;
    private final IngredientRepository ingredientRepository;
    private final CurrentUserService currentUserService;

    @Autowired
    public PriceAlertController(IngredientVendorPriceRepository vendorPriceRepository,
                                IngredientRepository ingredientRepository,
                                CurrentUserService currentUserService) {
        this.vendorPriceRepository = vendorPriceRepository;
        this.ingredientRepository = ingredientRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<PriceAlertResponse> getPriceAlerts() {
        Long ownerId = currentUserService.getEffectiveOwnerId();

        Map<Long, Ingredient> ownedIngredients = ingredientRepository.findAll().stream()
                .filter(i -> i.getOwnerId().equals(ownerId))
                .collect(Collectors.toMap(Ingredient::getId, i -> i));

        List<PriceAlertResponse> alerts = new ArrayList<>();

        for (IngredientVendorPrice ivp : vendorPriceRepository.findAll()) {
            Ingredient ingredient = ownedIngredients.get(ivp.getIngredient().getId());
            if (ingredient == null) continue;
            if (ivp.getPreviousPrice() == null || ivp.getPreviousPrice() <= 0) continue;

            double pctChange = ((ivp.getPurchasePrice() - ivp.getPreviousPrice()) / ivp.getPreviousPrice()) * 100.0;
            if (Math.abs(pctChange) < ALERT_THRESHOLD_PCT) continue;

            PriceAlertResponse dto = new PriceAlertResponse();
            dto.setIngredientId(ingredient.getId());
            dto.setIngredientName(ingredient.getName());
            dto.setVendorName(ivp.getVendor().getName());
            dto.setPreviousPrice(ivp.getPreviousPrice());
            dto.setCurrentPrice(ivp.getPurchasePrice());
            dto.setPercentChange(pctChange);
            dto.setLastUpdated(ivp.getLastUpdated() != null
                    ? ivp.getLastUpdated().format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                    : null);
            alerts.add(dto);
        }

        alerts.sort((a, b) -> Double.compare(Math.abs(b.getPercentChange()), Math.abs(a.getPercentChange())));
        return alerts;
    }
}