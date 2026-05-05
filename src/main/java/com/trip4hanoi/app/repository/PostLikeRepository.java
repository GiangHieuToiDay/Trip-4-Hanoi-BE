package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.Post;
import com.trip4hanoi.app.entity.PostLike;
import com.trip4hanoi.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostAndUser(Post post, User user);
    long countByPostId(Long postId);
    boolean existsByPostAndUser(Post post, User user);
}
