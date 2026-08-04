package grillogic.controller.dto;

import lombok.Data;

@Data
public class AdminClientRow {
    private Long userId;
    private String email;
    private String businessName;
    private String tier;
    private Boolean subscriptionActive;
    private Integer recipeCount;
    private Integer ingredientCount;
}