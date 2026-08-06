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

    // Null = this User IS a primary account owner — their own id is the "owner"
    // that Ingredients/Recipes/Vendors/etc. are scoped by.
    // Non-null = this User is a secondary login (e.g. a GM/manager) whose data
    // access resolves to the primary owner's account instead of their own id.
    @Column(name = "linked_owner_id")
    private Long linkedOwnerId;

    // "OWNER" for primary accounts, "MANAGER" for secondary logins under an owner.
    // Purely descriptive — access control is driven by linkedOwnerId, not this field.
    @Column(name = "account_role", nullable = false)
    private String accountRole = "OWNER";
}