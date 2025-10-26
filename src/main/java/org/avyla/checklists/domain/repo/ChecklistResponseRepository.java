package org.avyla.checklists.domain.repo;

import org.avyla.checklists.domain.model.ChecklistResponse;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChecklistResponseRepository extends JpaRepository<ChecklistResponse, Long> {

    @Query("""
           select r from ChecklistResponse r
           where r.instance.id = :instanceId and r.item.id = :itemId
           """)
    Optional<ChecklistResponse> findOneByInstanceAndItem(@Param("instanceId") Long instanceId,
                                                         @Param("itemId") Long itemId);

    List<ChecklistResponse> findByInstance_Id(Long instanceId);

    // Útil para evitar N+1 al leer severidad del ítem
    @Query("""
           select r from ChecklistResponse r
           join fetch r.item i
           where r.instance.id = :instanceId
           """)
    List<ChecklistResponse> findByInstance_IdWithItem(@Param("instanceId") Long instanceId);

    // Opcional (por si en algún punto necesitas buscar por itemCode)
    @Query("""
           select r from ChecklistResponse r
           join r.item i
           where r.instance.id = :instanceId and i.code = :itemCode
           """)
    Optional<ChecklistResponse> findByInstance_IdAndItemCode(@Param("instanceId") Long instanceId,
                                                             @Param("itemCode") String itemCode);

    @EntityGraph(attributePaths = {"item", "options", "options.option"})
    @Query("select r from ChecklistResponse r where r.instance.id = :instanceId")
    List<org.avyla.checklists.domain.model.ChecklistResponse> findGraphByInstanceId(@Param("instanceId") Long instanceId);
}
