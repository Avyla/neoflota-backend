package org.avyla.checklists.domain.entity;

import java.io.Serializable;
import java.util.Objects;

public class ChecklistResponseOptionId implements Serializable {
    private Long response; // response_id
    private Long option;   // option_id

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChecklistResponseOptionId that)) return false;
        return Objects.equals(response, that.response) && Objects.equals(option, that.option);
    }
    @Override public int hashCode() { return Objects.hash(response, option); }
}
