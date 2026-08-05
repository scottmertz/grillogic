package grillogic.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "vendor")
@Data
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String notes;

    // Which User (client) this vendor belongs to. Same ownership pattern as
    // Ingredient/Recipe — defaults to whoever is logged in when the vendor is created.
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
}