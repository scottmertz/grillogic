package grillogic.controller.dto;

import lombok.Data;

@Data
public class SubstituteResponse {
    private Long id;
    private Long substituteId;
    private String substituteName;
    private String substituteCategory;
    private Double substitutePricePerUnit; // cost per purchase unit, for quick comparison
    private String note;
}