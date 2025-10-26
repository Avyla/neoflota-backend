package org.avyla.checklists.domain.model;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.Instant;
import java.util.List;

@Entity @Table(name = "checklist_response",
        uniqueConstraints = @UniqueConstraint(columnNames = {"instance_id","item_id"}))
@Getter @Setter
public class ChecklistResponse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "response_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "instance_id", nullable = false)
    private ChecklistInstance instance;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "item_id", nullable = false)
    private ChecklistItem item;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "selected_option_id", nullable = false)
    private OptionItem selectedOption; // 'EstadoGeneral' option

    // Opciones de detalle seleccionadas para esta respuesta (catálogo del ítem)
    @OneToMany(mappedBy = "response",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<ChecklistResponseOption> options = new java.util.ArrayList<>();

    /**
     * Reemplaza completamente las opciones de detalle de esta respuesta.
     * newOptions: lista de OptionItem ya referenciables por id.
     */
    public void replaceOptions(java.util.Collection<OptionItem> newOptions) {
        // Eliminar las existentes (orphanRemoval = true)
        this.options.clear();

        if (newOptions == null || newOptions.isEmpty()) return;

        for (OptionItem opt : newOptions) {
            var ro = new ChecklistResponseOption();
            ro.setResponse(this);
            ro.setOption(opt);
            this.options.add(ro);
        }
    }


    @Column(length = 1000)
    private String comment;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}

