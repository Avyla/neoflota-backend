package org.avyla.checklists.domain.repo;

import org.avyla.checklists.domain.model.ChecklistInstance;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import jakarta.persistence.LockModeType;

public interface ChecklistInstanceRepository extends JpaRepository<ChecklistInstance, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ci from ChecklistInstance ci where ci.id = :id")
    Optional<ChecklistInstance> findByIdForUpdate(@Param("id") Long id);
}

