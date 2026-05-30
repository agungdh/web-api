package id.my.agungdh;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "comment")
public class Comment extends BaseEntity {

    @Column(nullable = false)
    public String name;

    public String email;

    @Column(nullable = false, columnDefinition = "TEXT")
    public String content;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    public Post post;
}
