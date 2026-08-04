package grillogic.controller.dto;

import grillogic.model.Ingredient;
import lombok.Data;

import java.util.List;

@Data
public class AdminClientDetailResponse {
    private Long userId;
    private String email;
    private String businessName;
    private String tier;
    private Boolean subscriptionActive;
    private List<RecipeSummaryResponse> recipes;
    private List<Ingredient> ingredients;
}
