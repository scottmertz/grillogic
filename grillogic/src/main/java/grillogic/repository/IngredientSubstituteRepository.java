package grillogic.repository;

import grillogic.model.IngredientSubstitute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredientSubstituteRepository extends JpaRepository<IngredientSubstitute, Long> {
    List<IngredientSubstitute> findByIngredientId(Long ingredientId);
}