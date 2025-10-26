package org.avyla.checklists.domain.repo;

import org.avyla.checklists.domain.model.ChecklistInstance;
import org.avyla.checklists.infrastructure.InstanceStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChecklistInstanceRepository extends JpaRepository<ChecklistInstance, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from ChecklistInstance i where i.id = :id")
    Optional<ChecklistInstance> findByIdForUpdate(@Param("id") Long id);

    @Query("""
           select i from ChecklistInstance i
           where i.driverId = :driverId
             and i.status in :statuses
             and (i.dueAt is null or i.dueAt >= :now)
             and i.completedAt is null
           order by i.startedAt asc
           """)
    List<ChecklistInstance> findOpenNotExpiredByDriver(@Param("driverId") Long driverId,
                                                       @Param("statuses") List<String> statuses,
                                                       @Param("now") Instant now);

    @Query("""
           select i from ChecklistInstance i
           where i.driverId = :driverId
             and i.status = 'EXPIRED'
           order by coalesce(i.completedAt, i.dueAt) desc
           """)
    Optional<ChecklistInstance> findLastExpiredByDriver(@Param("driverId") Long driverId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
        UPDATE ChecklistInstance i
           SET i.status = :expired,
               i.completedAt = :now
         WHERE i.status IN :open
           AND i.dueAt <= :now
           AND (i.completedAt IS NULL OR i.status <> :expired)
    """)
    int expireDueInstances(@Param("now") Instant now,
                           @Param("open") List<InstanceStatus> open,
                           @Param("expired") InstanceStatus expired);

    // (opcional) si prefieres paginar y procesar en memoria:
    @Query("""
        SELECT i FROM ChecklistInstance i
         WHERE i.status IN :open AND i.dueAt <= :now
    """)
    List<ChecklistInstance> findAllDue(@Param("now") Instant now,
                                       @Param("open") List<InstanceStatus> open);
}
