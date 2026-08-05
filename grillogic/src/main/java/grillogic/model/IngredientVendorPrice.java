package grillogic.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

// A single vendor's quoted price for a specific ingredient. An ingredient can have
// multiple quotes (one per vendor) so prices can be compared side by side. Exactly
// one quote per ingredient can be marked isPreferred=true — that's the one
// CostingService actually uses when calculating recipe costs.
@Entity
@Table(name = "ingredient_vendor_price")
@Data
public class IngredientVendorPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @ManyToOne
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    // Mirrors Ingredient's own purchase fields, so a quote like "$42 for a 42lb case"
    // can be compared apples-to-apples against another vendor's different case size.
    @Column(name = "purchase_price", nullable = false)
    private Double purchasePrice;

    @Column(name = "purchase_amount", nullable = false)
    private Double purchaseAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_unit", nullable = false)
    private Unit purchaseUnit;

    @Column(name = "is_preferred", nullable = false)
    private Boolean isPreferred = false;

    // Captured automatically whenever purchasePrice changes on an update —
    // this is what Step 13d (price change alerts) will compare against.
    @Column(name = "previous_price")
    private Double previousPrice;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}