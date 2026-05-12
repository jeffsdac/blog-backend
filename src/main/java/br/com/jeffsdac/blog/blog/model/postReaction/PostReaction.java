package br.com.jeffsdac.blog.blog.model.postReaction;

import java.util.UUID;

import br.com.jeffsdac.blog.blog.model.BaseModelClass;
import br.com.jeffsdac.blog.blog.model.postReaction.enums.ReactionType;
import br.com.jeffsdac.blog.blog.model.posts.PostModel;
import br.com.jeffsdac.blog.blog.model.userBlog.UserBlog;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@Table(name = "T_BLOG_POST_REACTION", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "post_id", "user_id" })
}

)
public class PostReaction extends BaseModelClass {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private PostModel postModel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserBlog userBlog;

    public PostReaction() {

    }

    public PostReaction(PostModel post, UserBlog user, ReactionType reactionType) {
        this.userBlog = user;
        this.postModel = post;
        this.type = reactionType;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ReactionType getType() {
        return type;
    }

    public void setType(ReactionType type) {
        this.type = type;
    }

    public PostModel getPostModel() {
        return postModel;
    }

    public void setPostModel(PostModel postModel) {
        this.postModel = postModel;
    }

    public UserBlog getUserBlog() {
        return userBlog;
    }

    public void setUserBlog(UserBlog userBlog) {
        this.userBlog = userBlog;
    }

}
