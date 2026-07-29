package grillogic.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ingredient")
@Data
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // The unit this ingredient is PURCHASED in (e.g. a case of ketchup bags is LB)
    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_unit", nullable = false)
    private Unit purchaseUnit;

    // How much you paid, in purchaseUnit terms (e.g. $42.00 for the case)
    @Column(name = "purchase_price", nullable = false)
    private Double purchasePrice;

    // How much purchaseUnit you got for that price (e.g. 42 lb case)
    @Column(name = "purchase_amount", nullable = false)
    private Double purchaseAmount;

    // Which cost/menu category this belongs to (protein, produce, dairy, etc.)
    private String category;

    // Yield % after trim/cook loss — 1.0 = no loss, 0.85 = 15% loss
    @Column(name = "yield_pct")
    private Double yieldPct;
}
