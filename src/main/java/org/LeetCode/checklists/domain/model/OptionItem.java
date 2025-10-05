package org.LeetCode.checklists.domain.model;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;

@Entity @Table(name = "option_item",
        uniqueConstraints = @UniqueConstraint(columnNames = {"option_group_id","code"}))
@Getter @Setter
public class OptionItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "option_group_id", nullable = false)
    private OptionGroup group;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 120)
    private String label;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}

