package org.avyla.checklists.domain.repo;

import org.avyla.checklists.domain.entity.OptionItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OptionItemRepository extends JpaRepository<OptionItem, Long> {

    // Id del OptionItem dentro del grupo 'EstadoGeneral' (OK/OBS/NOOP/NA)
    @Query("""
           select oi.id from OptionItem oi
           where oi.group.code = 'EstadoGeneral' and oi.code = :code and oi.active = true
           """)
    Optional<Long> findEstadoGeneralId(@Param("code") String code);

    // Id de un OptionItem por group_id + code (para los detalles de item)
    @Query("""
           select oi.id from OptionItem oi
           where oi.group.id = :groupId and oi.code = :code and oi.active = true
           """)
    Optional<Long> findIdByGroupIdAndCode(@Param("groupId") Long groupId,
                                          @Param("code") String code);

    // Lista completa de un grupo por su code (ordenada)
    @Query("""
           select oi from OptionItem oi
           where oi.group.code = :groupCode and oi.active = true
           order by coalesce(oi.orderIndex, 0) asc, oi.id asc
           """)
    List<OptionItem> findActiveByGroupCode(@Param("groupCode") String groupCode);

    // Proyección liviana usada por ChecklistTemplateQueryService
    @Query("""
           select oi.group.code as groupCode,
                  oi.code        as code,
                  oi.label       as label,
                  oi.orderIndex  as orderIndex
           from OptionItem oi
           where oi.group.code in :groupCodes
             and oi.active = true
           order by oi.group.code asc, coalesce(oi.orderIndex,0) asc, oi.code asc
           """)
    List<OptionView> findActiveViewsByGroupCodes(@Param("groupCodes") List<String> groupCodes);

    interface OptionView {
        String getGroupCode();
        String getCode();
        String getLabel();
        Integer getOrderIndex();
    }
}
