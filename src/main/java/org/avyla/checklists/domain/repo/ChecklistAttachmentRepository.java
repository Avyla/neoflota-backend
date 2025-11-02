package org.avyla.checklists.domain.repo;

import org.avyla.checklists.domain.entity.ChecklistAttachment;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface ChecklistAttachmentRepository extends JpaRepository<ChecklistAttachment, UUID> {

    boolean existsByResponse_Id(Long responseId);

    List<ChecklistAttachment> findAllByResponse_Id(Long responseId);

    @Modifying
    @Query("delete from ChecklistAttachment a " +
            "where a.instance.id = :instanceId and a.response is null")
    void deleteInstanceAttachments(@Param("instanceId") Long instanceId);
}
