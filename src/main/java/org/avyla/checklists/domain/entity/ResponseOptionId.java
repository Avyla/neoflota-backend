package org.avyla.checklists.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ResponseOptionId implements Serializable {

    @Column(name = "response_id", nullable = false)
    private Long responseId;

    @Column(name = "option_id", nullable = false)
    private Long optionId;
}
