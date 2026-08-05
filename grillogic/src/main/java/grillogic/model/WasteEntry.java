package grillogic.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "waste_entry")
@Data
public class WasteEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    @ManyToOne
    @JoinColumn(name = "sub_recipe_id")
    private Recipe subRecipe;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "amount_unit", nullable = false)
    private Unit amountUnit;

    @Column(nullable = false)
    private LocalDate wasteDate;

    private String reason; // optional — e.g. "spoiled", "over-prepped", "dropped"
}