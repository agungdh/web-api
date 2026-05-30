package id.my.agungdh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "\"user\"")
public class User extends BaseEntity {

    @Column(nullable = false)
    public String name;

    @Column(nullable = false, unique = true)
    public String email;
}
