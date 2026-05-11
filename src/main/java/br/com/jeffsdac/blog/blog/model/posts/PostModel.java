package br.com.jeffsdac.blog.blog.model.posts;

import java.util.UUID;

import br.com.jeffsdac.blog.blog.model.BaseModelClass;
import br.com.jeffsdac.blog.blog.model.userBlog.UserBlog;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "T_BLOG_POST")
public class PostModel extends BaseModelClass {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private UserBlog author;

    @Column(nullable = false)
    private int likeVotes = 0;

    @Column(nullable = false)
    private int unlikeVotes = 0;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UserBlog getAuthor() {
        return author;
    }

    public void setAuthor(UserBlog author) {
        this.author = author;
    }

    public int getLikeVotes() {
        return likeVotes;
    }

    public void setLikeVotes(int likeVotes) {
        this.likeVotes = likeVotes;
    }

    public int getUnlikeVotes() {
        return unlikeVotes;
    }

    public void setUnlikeVotes(int unlikeVotes) {
        this.unlikeVotes = unlikeVotes;
    }
}

