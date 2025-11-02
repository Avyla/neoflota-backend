package org.avyla.vehicles.domain.repo;

import org.avyla.vehicles.domain.entity.VehicleFuelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VehicleFuelTypeRepository extends JpaRepository<VehicleFuelType, Long> {

    Optional<VehicleFuelType> findByName(String name);

    @Query("SELECT f FROM VehicleFuelType f WHERE f.active = true ORDER BY f.name")
    List<VehicleFuelType> findAllActive();

}
