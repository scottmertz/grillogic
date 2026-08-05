package grillogic.controller.dto;

import lombok.Data;

@Data
public class SubstituteRequest {
    private Long ingredientId;
    private Long substituteId;
    private String note;
}