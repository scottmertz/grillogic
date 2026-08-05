package grillogic.controller.dto;

import grillogic.model.Unit;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WasteEntryRequest {
    private Long ingredientId;
    private Long subRecipeId;
    private Double amount;
    private Unit amountUnit;
    private LocalDate wasteDate;
    private String reason;
}