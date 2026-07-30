package grillogic.controller.dto;

import lombok.Data;

@Data
public class RecipeResponse {
    private Long id;
    private String name;
    private Integer servings;
    private Double menuPrice;
}