package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.res.PostLikeResponse;
import java.util.List;

public interface PostLikeService {
    void toggleLike(Long postId);
    List<PostLikeResponse> getLikesByPostId(Long postId);
    long countLikesByPostId(Long postId);
    boolean isPostLikedByUser(Long postId, Long userId);
}
