package org.avyla.checklists.domain.repo;

import org.avyla.checklists.domain.model.ChecklistResponseOption;
import org.avyla.checklists.domain.model.ResponseOptionId;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChecklistResponseOptionRepository extends JpaRepository<ChecklistResponseOption, ResponseOptionId> {


    void deleteByResponse_Id(Long responseId);

    List<ChecklistResponseOption> findAllByResponse_Id(Long responseId);

}
