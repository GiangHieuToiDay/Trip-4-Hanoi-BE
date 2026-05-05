package com.trip4hanoi.app.service;

public interface PostLikeService {
    void toggleLike(Long postId);
    long countLikes(Long postId);
    boolean isLikedByUser(Long postId, Long userId);
}
