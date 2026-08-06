package grillogic.controller.dto;

import lombok.Data;

@Data
public class AddManagerRequest {
    private String email;
    private String password;
}