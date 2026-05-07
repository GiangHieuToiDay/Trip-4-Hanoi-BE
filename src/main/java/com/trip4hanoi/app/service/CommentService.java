package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.req.CommentRequest;
import com.trip4hanoi.app.dto.res.CommentResponse;
import com.trip4hanoi.app.dto.res.PageResponse;

public interface CommentService {
    CommentResponse createComment(CommentRequest request);
    CommentResponse updateComment(Long commentId, String content);
    void deleteComment(Long commentId);
    PageResponse<CommentResponse> getCommentsByPost(Long postId, int page, int size);
    PageResponse<CommentResponse> getAllComments(int page, int size);
}
