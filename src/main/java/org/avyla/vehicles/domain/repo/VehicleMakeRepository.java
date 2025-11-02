package org.avyla.vehicles.domain.repo;

import org.avyla.vehicles.domain.entity.VehicleMake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VehicleMakeRepository extends JpaRepository<VehicleMake, Long> {

    Optional<VehicleMake> findByName(String name);

    @Query("SELECT m FROM VehicleMake m WHERE m.active = true ORDER BY m.name")
    List<VehicleMake> findAllActive();

}
