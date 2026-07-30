package grillogic.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipe")
@Data
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // How many portions this recipe yields (e.g. a batch of chili makes 20 servings)
    @Column(nullable = false)
    private Integer servings;

    // Menu price for this dish, if it's a sellable item
    @Column(name = "menu_price")
    private Double menuPrice;

    // Labor cost as a percentage of menu price (e.g. 0.30 = 30%)
    @Column(name = "labor_cost_pct")
    private Double laborCostPct;

    // The list of ingredients (and amounts) that make up this recipe.
    // "mappedBy" tells Hibernate the RecipeIngredient.recipe field owns the actual foreign key —
    // this side is just the reverse lookup, no extra column created here.
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    // Only needed if this recipe will be used AS an ingredient in another recipe
    @Column(name = "batch_yield_amount")
    private Double batchYieldAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "batch_yield_unit")
    private Unit batchYieldUnit;

    // Optional — only used if this operator pays DEDICATED labor for this
    // recipe/prep (e.g. a paid pit boss), as opposed to labor blended into a
    // normal shift. Null/0 means no dedicated labor cost is added.
    @Column(name = "dedicated_labor_hours")
    private Double dedicatedLaborHours;

    @Column(name = "dedicated_labor_rate")
    private Double dedicatedLaborRate;
}