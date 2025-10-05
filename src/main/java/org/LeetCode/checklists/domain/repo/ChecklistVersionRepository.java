package org.LeetCode.checklists.domain.repo;

import org.LeetCode.checklists.domain.model.ChecklistVersion;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChecklistVersionRepository extends JpaRepository<ChecklistVersion, Long> {

    @Query("""
    select v.id from ChecklistVersion v
      join v.template t
    where t.code = :templateCode
      and v.versionLabel = :versionLabel
      and v.status = 'Published'
  """)
    Optional<Long> findPublishedVersionId(@Param("templateCode") String templateCode,
                                          @Param("versionLabel") String versionLabel);

}

