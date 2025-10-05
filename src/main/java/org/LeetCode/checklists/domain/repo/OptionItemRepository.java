package org.LeetCode.checklists.domain.repo;

import org.LeetCode.checklists.domain.model.OptionItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OptionItemRepository extends JpaRepository<OptionItem, Long> {

    // Estados de EstadoGeneral: OK/OBS/NOOP/NA
    @Query("""
    select oi.id from OptionItem oi
      join oi.group g
    where g.code = 'EstadoGeneral' and oi.code = :code
  """)
    Optional<Long> findEstadoGeneralId(@Param("code") String code);


    @Query("""
    select oi.id from OptionItem oi
      where oi.group.id = :groupId and oi.code = :optionCode
  """)
    Optional<Long> findIdByGroupIdAndCode(@Param("groupId") Long groupId,
                                          @Param("optionCode") String optionCode);

    // Buscar opción por grupo y code (para detalles multiselect)
    @Query("""
    select oi.id from OptionItem oi
      join oi.group g
    where g.code = :groupCode and oi.code = :optionCode
  """)
    Optional<Long> findOptionId(@Param("groupCode") String groupCode,
                                @Param("optionCode") String optionCode);
}

