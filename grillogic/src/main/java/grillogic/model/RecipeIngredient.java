package grillogic.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "recipe_ingredient")
@Data
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which recipe this line belongs to
    @ManyToOne
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    // Which ingredient is being used
    @ManyToOne
    @JoinColumn(name = "ingredient_id", nullable = true)
    private Ingredient ingredient;

    @ManyToOne
    @JoinColumn(name = "sub_recipe_id", nullable = true)
    private Recipe subRecipe;

    // How much of that ingredient this recipe uses
    @Column(nullable = false)
    private Double amount;

    // What unit that amount is expressed in (may differ from the ingredient's purchase unit)
    @Enumerated(EnumType.STRING)
    @Column(name = "amount_unit", nullable = false)
    private Unit amountUnit;
}
