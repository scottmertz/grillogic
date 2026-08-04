package grillogic.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "app_user")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role; // "ADMIN" or "CLIENT"

    // Business name shown on reports/admin panel — e.g. "Treehouse Tavern"
    private String businessName;

    // "NONE", "AUDIT_ONLY", "STANDARD", or "PREMIUM" — drives report tier-gating
    // and what shows in the Admin Panel's subscription column
    @Column(nullable = false)
    private String tier = "NONE";

    private Boolean subscriptionActive = false;
}