package id.my.agungdh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "category")
@SequenceGenerator(name = "sequence_gen", sequenceName = "category_id_seq", allocationSize = 1)
public class Category extends BaseEntity {

    @Column(nullable = false)
    public String name;

    @Column(nullable = false, unique = true)
    public String slug;
}
