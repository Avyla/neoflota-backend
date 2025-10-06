package org.LeetCode.checklists.domain.repo;

import org.LeetCode.checklists.domain.model.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {

    // Derivado que ya tenías (puede servir para otros listados)
    List<ChecklistItem> findByVersion_IdOrderBySection_OrderIndexAscOrderIndexAsc(Long versionId);

    /** Proyección usada por el servicio Published */
    interface ItemView {
        String getCode();
        String getLabel();
        Boolean getRequired();
        Boolean getAllowNa();
        String getSeverity();
        Integer getOrderIndex();
        String getDetailCatalogCode();
        Long getSectionId();
        String getHelpText();
    }

    @Query("""
        select ci.code as code,
               ci.label as label,
               ci.required as required,
               ci.allowNa as allowNa,
               ci.severity as severity,
               ci.orderIndex as orderIndex,
               dog.code as detailCatalogCode,
               s.id as sectionId,
               ci.helpText as helpText
        from ChecklistItem ci
        left join ci.section s
        left join ci.detailOptionGroup dog
        where ci.version.id = :versionId
        order by ci.orderIndex asc
    """)
    List<ItemView> findViewsByVersionId(@Param("versionId") Long versionId);
}
