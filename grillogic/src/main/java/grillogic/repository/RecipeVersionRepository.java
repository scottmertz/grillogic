package grillogic.repository;

import grillogic.model.RecipeVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeVersionRepository extends JpaRepository<RecipeVersion, Long> {
    List<RecipeVersion> findByRecipeIdOrderByVersionTimestampDesc(Long recipeId);
}