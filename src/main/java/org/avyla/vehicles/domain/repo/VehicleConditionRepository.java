package org.avyla.vehicles.domain.repo;

import org.avyla.vehicles.domain.model.VehicleCondition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleConditionRepository extends JpaRepository<VehicleCondition, Long> {
    Optional<VehicleCondition> findByCode(String code);
}
