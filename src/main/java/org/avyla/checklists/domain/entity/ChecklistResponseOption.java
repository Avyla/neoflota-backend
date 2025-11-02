package org.avyla.checklists.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "checklist_response_option")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ChecklistResponseOption {

    @EmbeddedId
    private ResponseOptionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("responseId") // enlaza con id.responseId
    @JoinColumn(name = "response_id", nullable = false)
    private ChecklistResponse response;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("optionId")   // enlaza con id.optionId
    @JoinColumn(name = "option_id", nullable = false)
    private OptionItem option;
}
