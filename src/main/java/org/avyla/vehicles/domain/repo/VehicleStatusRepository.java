package org.avyla.vehicles.domain.repo;

import org.avyla.vehicles.domain.model.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleStatusRepository extends JpaRepository<VehicleStatus, Long> {

    Optional<VehicleStatus> findByCode(String code);

    @Query("SELECT s FROM VehicleStatus s WHERE s.active = true ORDER BY s.name")
    List<VehicleStatus> findAllActive();

}