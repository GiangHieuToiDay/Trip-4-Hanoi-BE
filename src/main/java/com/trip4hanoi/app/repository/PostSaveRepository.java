package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.Post;
import com.trip4hanoi.app.entity.PostSave;
import com.trip4hanoi.app.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostSaveRepository extends JpaRepository<PostSave, Long> {
    Page<PostSave> findByUserId(Long userId, Pageable pageable);
    
    // Thêm các phương thức Service đang dùng
    List<PostSave> findByUser(User user);
    Optional<PostSave> findByUserAndPost(User user, Post post);
    boolean existsByUserAndPost(User user, Post post);
    
    Optional<PostSave> findByPostIdAndUserId(Long postId, Long userId);
    boolean existsByPostIdAndUserId(Long postId, Long userId);
}
