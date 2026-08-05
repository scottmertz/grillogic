package grillogic.controller.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class WasteEntryResponse {
    private Long id;
    private String itemName;
    private String itemType; // "Ingredient" or "Sub-Recipe"
    private Double amount;
    private String unit;
    private Double cost;
    private LocalDate wasteDate;
    private String reason;
}