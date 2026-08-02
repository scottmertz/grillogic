package grillogic.controller.dto;

import lombok.Data;

@Data
public class ReportRow {
    private String name;
    private String menuPriceDisplay;
    private String costPerServingDisplay;
    private String foodCostPctDisplay;
    private String statusLabel;
    private String statusClass; // "status-good" | "status-warn" | "status-bad"
    private String rowClass;    // "row-good" | "row-warn" | "row-bad"
    private String suggestedPriceDisplay;
}