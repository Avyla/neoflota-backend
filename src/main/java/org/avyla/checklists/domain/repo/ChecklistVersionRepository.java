package org.avyla.checklists.domain.repo;

import org.avyla.checklists.domain.entity.ChecklistVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChecklistVersionRepository extends JpaRepository<ChecklistVersion, Long> {

    // Opción derivada (si tu modelo lo soporta)
    Optional<ChecklistVersion> findTopByTemplate_CodeAndStatusOrderByPublishedAtDesc(String templateCode, String status);

    // Opción con @Query (fallback)
    @Query("""
        select v
        from ChecklistVersion v
        join v.template t
        where t.code = :templateCode and v.status = 'Published'
        order by v.publishedAt desc
    """)
    Optional<ChecklistVersion> findLatestPublishedByTemplateCode(@Param("templateCode") String templateCode);

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
