package org.LeetCode.checklists.domain.repo;


import org.LeetCode.checklists.domain.model.ChecklistResponseOption;
import org.LeetCode.checklists.domain.model.ChecklistResponseOptionId;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChecklistResponseOptionRepository
        extends JpaRepository<ChecklistResponseOption, ChecklistResponseOptionId> {

    @Modifying
    @Query("delete from ChecklistResponseOption ro where ro.response.id = :responseId")
    void deleteByResponseId(@Param("responseId") Long responseId);
}

