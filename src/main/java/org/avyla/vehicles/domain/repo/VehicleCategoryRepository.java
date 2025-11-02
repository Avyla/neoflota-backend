package org.avyla.vehicles.domain.repo;

import org.avyla.vehicles.domain.entity.VehicleCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VehicleCategoryRepository extends JpaRepository<VehicleCategory, Long> {

    Optional<VehicleCategory> findByName(String name);

    @Query("SELECT c FROM VehicleCategory c WHERE c.active = true ORDER BY c.name")
    List<VehicleCategory> findAllActive();

}
