package grillogic.repository;

import grillogic.model.IngredientVendorPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngredientVendorPriceRepository extends JpaRepository<IngredientVendorPrice, Long> {

    List<IngredientVendorPrice> findByIngredientId(Long ingredientId);

    Optional<IngredientVendorPrice> findByIngredientIdAndIsPreferredTrue(Long ingredientId);
}
