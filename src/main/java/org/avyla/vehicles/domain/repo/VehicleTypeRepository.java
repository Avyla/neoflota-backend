package org.avyla.vehicles.domain.repo;

import org.avyla.vehicles.domain.model.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VehicleTypeRepository extends JpaRepository<VehicleType, Long> {

    Optional<VehicleType> findByName(String name);

    @Query("SELECT t FROM VehicleType t WHERE t.active = true ORDER BY t.name")
    List<VehicleType> findAllActive();

}
