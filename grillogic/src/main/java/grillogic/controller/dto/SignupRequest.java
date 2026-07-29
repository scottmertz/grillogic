package grillogic.controller.dto;

import lombok.Data;

@Data
public class SignupRequest {
    private String email;
    private String password; // raw password from the form — never stored as-is
}