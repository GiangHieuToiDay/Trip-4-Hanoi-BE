package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.req.CommentRequest;
import com.trip4hanoi.app.dto.res.CommentResponse;

import java.util.List;

public interface CommentService {
    CommentResponse createComment(CommentRequest request);
    CommentResponse updateComment(Long commentId, String content);
    void deleteComment(Long commentId);
    List<CommentResponse> getCommentsByPost(Long postId);
}
