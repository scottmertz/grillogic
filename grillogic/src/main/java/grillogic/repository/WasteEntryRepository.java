package grillogic.repository;

import grillogic.model.WasteEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WasteEntryRepository extends JpaRepository<WasteEntry, Long> {
}