package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.Post;
import com.trip4hanoi.app.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAll(Pageable pageable);
    Post findById(long id);
    Page<Post> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    void deleteById(long id);
    List<Post> findPostByUser(User user);
}
