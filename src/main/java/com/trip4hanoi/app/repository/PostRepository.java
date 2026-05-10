package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.Post;
import com.trip4hanoi.app.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAll(Pageable pageable);
    Optional<Post> findById(long id);
    Page<Post> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    void deleteById(long id);
    List<Post> findPostByUser(User user);


    //=============================================================================================
    //DASHBOARD

    // Tìm bài viết có tỉ lệ lan tỏa cao nhất
    @Query(value = "SELECT p.id, p.title, u.username, " +
            "((SELECT COUNT(*) FROM post_likes WHERE post_id = p.id) + (SELECT COUNT(*) FROM post_saves WHERE post_id = p.id)) / NULLIF(p.view_count, 0) as viral_rate " +
            "FROM posts p JOIN users u ON p.user_id = u.id " +
            "ORDER BY viral_rate DESC LIMIT 10", nativeQuery = true)
    List<Object[]> findTopViralPosts();

    // Thống kê bài viết mới theo tháng
    @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-%m') as month, COUNT(*) FROM posts GROUP BY month ORDER BY month DESC", nativeQuery = true)
    List<Object[]> getPostGrowthByMonth();
}
