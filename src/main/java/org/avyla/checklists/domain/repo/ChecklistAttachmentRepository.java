package org.avyla.checklists.domain.repo;


import org.avyla.checklists.domain.model.ChecklistAttachment;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ChecklistAttachmentRepository extends JpaRepository<ChecklistAttachment, Long> {
    boolean existsByResponse_Id(Long responseId);

    @Modifying
    @Query("delete from ChecklistAttachment a where a.instance.id = :instanceId and a.response is null")
    void deleteInstanceAttachments(@Param("instanceId") Long instanceId); // por si quieres limpiar antes de re-subir
}

