package id.my.agungdh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "post")
public class Post extends BaseEntity {

    @Column(nullable = false)
    public String title;

    @Column(nullable = false, unique = true)
    public String slug;

    @Column(columnDefinition = "TEXT")
    public String content;

    @Column(name = "published_at")
    public Instant publishedAt;

    @ManyToOne
    @JoinColumn(name = "category_id")
    public Category category;

    @ManyToMany
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    public Set<Tag> tags = new HashSet<>();
}
