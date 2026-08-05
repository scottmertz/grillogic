package grillogic.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ingredient_substitute")
@Data
public class IngredientSubstitute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    // The original ingredient
    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    // The suggested substitute
    @ManyToOne
    @JoinColumn(name = "substitute_id", nullable = false)
    private Ingredient substitute;

    private String note; // e.g. "Cheaper, similar flavor profile" or "Use if out of stock"
}