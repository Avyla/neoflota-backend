package org.avyla.vehicles.domain.repo;

import org.avyla.vehicles.domain.entity.VehicleCondition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleConditionRepository extends JpaRepository<VehicleCondition, Long> {

    List<VehicleCondition> findAll();
    Optional<VehicleCondition> findByCode(String code);

}
