package grillogic.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "recipe_version")
@Data
public class RecipeVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    @Column(name = "version_timestamp", nullable = false)
    private LocalDateTime versionTimestamp;

    // A JSON snapshot of the recipe's full state at this point in time —
    // name, servings, pricing, and every ingredient line, exactly as it looked
    // right before this version was overwritten.
    @Column(columnDefinition = "TEXT", nullable = false)
    private String snapshotJson;
}