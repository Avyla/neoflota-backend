package org.avyla.checklists.domain.repo;

import org.avyla.checklists.domain.model.ChecklistResponse;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ChecklistResponseRepository extends JpaRepository<ChecklistResponse, Long> {
    List<ChecklistResponse> findByInstance_Id(Long instanceId);

    @Query("""
    select r from ChecklistResponse r
     where r.instance.id = :instanceId and r.item.id = :itemId
  """)
    Optional<ChecklistResponse> findOneByInstanceAndItem(@Param("instanceId") Long instanceId,
                                                         @Param("itemId") Long itemId);
}


