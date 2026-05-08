package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.Post;
import com.trip4hanoi.app.entity.PostLike;
import com.trip4hanoi.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    // Thêm các phương thức Service đang dùng
    Optional<PostLike> findByUserAndPost(User user, Post post);
    List<PostLike> findByPostId(Long postId);
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    Optional<PostLike> findByPostAndUser(Post post, User user);
    long countByPostId(Long postId);
    boolean existsByPostAndUser(Post post, User user);
}
