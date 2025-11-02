package org.avyla.checklists.domain.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;

@Entity @Table(name = "checklist_item",
        uniqueConstraints = @UniqueConstraint(columnNames = {"version_id","code"}))
@Getter @Setter
public class ChecklistItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "version_id", nullable = false)
    private ChecklistVersion version;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "section_id")
    private ChecklistSection section;

    @Column(nullable = false, length = 50)
    private String code; // ej. ROD_LLANTAS

    @Column(nullable = false, length = 200)
    private String label;

    @Column(name = "help_text", length = 500)
    private String helpText;

    @Column(nullable = false, length = 10)
    private String severity; // 'Low'|'Medium'|'High'|'Critical'

    @Column(nullable = false, name="required")
    private boolean required;

    @Column(nullable = false, name="allow_na")
    private boolean allowNa;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "state_option_group_id", nullable = false)
    private OptionGroup stateOptionGroup; // siempre 'EstadoGeneral'

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "detail_option_group_id")
    private OptionGroup detailOptionGroup; // null si no aplica

    @Column(name = "order_index", nullable = false)
    private int orderIndex;
}

