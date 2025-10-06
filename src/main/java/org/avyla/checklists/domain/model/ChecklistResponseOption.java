package org.avyla.checklists.domain.model;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;

@Entity @Table(name = "checklist_response_option")
@IdClass(ChecklistResponseOptionId.class)
@Getter @Setter
public class ChecklistResponseOption {
    @Id
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "response_id")
    private ChecklistResponse response;

    @Id
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "option_id")
    private OptionItem option;
}

