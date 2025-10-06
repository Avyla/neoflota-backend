package org.avyla.checklists.domain.repo;

import org.avyla.checklists.domain.model.OptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OptionItemRepository extends JpaRepository<OptionItem, Long> {

    /**
     * Devuelve las opciones activas de un grupo (por code del grupo), ordenadas por orderIndex.
     * OJO: en la entidad la propiedad booleana se llama 'active' (no 'isActive').
     * La asociación hacia OptionGroup se llama 'group'.
     */
    @Query("""
        select oi
        from OptionItem oi
        join oi.group og
        where og.code = :groupCode
          and oi.active = true
        order by oi.orderIndex asc
    """)
    List<OptionItem> findActiveByGroupCode(@Param("groupCode") String groupCode);

    /**
     * Proyección para no depender de getters de la entidad: expone el code del grupo como 'groupCode'.
     */
    interface OptionView {
        String getGroupCode();
        String getCode();
        String getLabel();
        Integer getOrderIndex();
    }

    /**
     * Devuelve vistas (OptionView) de múltiples grupos a la vez, ordenadas por grupo y orderIndex.
     * Usa 'oi.group' y la propiedad booleana 'active'.
     */
    @Query("""
        select og.code as groupCode,
               oi.code  as code,
               oi.label as label,
               oi.orderIndex as orderIndex
        from OptionItem oi
        join oi.group og
        where og.code in :groupCodes
          and oi.active = true
        order by og.code asc, oi.orderIndex asc
    """)
    List<OptionView> findActiveViewsByGroupCodes(@Param("groupCodes") List<String> groupCodes);

    @Query("""
        select oi.id
        from OptionItem oi
        where oi.group.id = :groupId and oi.code = :optionCode
    """)
    Optional<Long> findIdByGroupIdAndCode(@Param("groupId") Long groupId,
                                          @Param("optionCode") String optionCode);

    // Estados de EstadoGeneral: OK/OBS/NOOP/NA
    @Query("""
        select oi.id
        from OptionItem oi
        join oi.group g
        where g.code = 'EstadoGeneral' and oi.code = :code
    """)
    Optional<Long> findEstadoGeneralId(@Param("code") String code);
}
