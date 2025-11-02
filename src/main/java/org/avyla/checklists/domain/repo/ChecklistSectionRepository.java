package org.avyla.checklists.domain.repo;

import org.avyla.checklists.domain.entity.ChecklistSection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChecklistSectionRepository extends Repository<ChecklistSection, Long> {

    // Derivado existente (útil para otros casos)
    List<ChecklistSection> findByVersion_IdOrderByOrderIndexAsc(Long versionId);

    /** Proyección usada por el servicio Published */
    interface SectionView {
        Long getId();
        String getCode();
        String getTitle();
        Integer getOrderIndex();
    }

    @Query("""
        select s.id as id, s.code as code, s.title as title, s.orderIndex as orderIndex
        from ChecklistSection s
        where s.version.id = :versionId
        order by s.orderIndex asc
    """)
    List<SectionView> findViewsByVersionId(@Param("versionId") Long versionId);
}
