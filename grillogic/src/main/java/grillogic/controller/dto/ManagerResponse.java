package grillogic.controller.dto;

import lombok.Data;

@Data
public class ManagerResponse {
    private Long id;
    private String email;
    private String accountRole;
}