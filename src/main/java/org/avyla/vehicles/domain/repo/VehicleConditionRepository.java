package org.avyla.vehicles.domain.repo;

import org.avyla.common.util.ConditionOptions;
import org.avyla.vehicles.domain.model.VehicleCondition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleConditionRepository extends JpaRepository<VehicleCondition, Long> {

    List<VehicleCondition> findAll();
    Optional<VehicleCondition> findByCode(String code);

}
