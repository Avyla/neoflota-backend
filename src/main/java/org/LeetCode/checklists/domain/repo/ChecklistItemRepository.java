package org.LeetCode.checklists.domain.repo;

import org.LeetCode.checklists.domain.model.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {
    List<ChecklistItem> findByVersion_IdOrderBySection_OrderIndexAscOrderIndexAsc(Long versionId);

    @Query("""
    select i from ChecklistItem i
     where i.version.id = :versionId and i.code in :codes
  """)
    List<ChecklistItem> findByVersionAndCodes(@Param("versionId") Long versionId,
                                              @Param("codes") List<String> codes);

    @Query("""
    select i from ChecklistItem i
     where i.version.id = :versionId and i.code = :code
  """)
    Optional<ChecklistItem> findOneByVersionAndCode(@Param("versionId") Long versionId,
                                                    @Param("code") String code);
}

