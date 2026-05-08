package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.dto.res.PostSaveResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface PostSaveService {
    PostSaveResponse savePost(Long postId);
    void unsavePost(Long postId);
    PageResponse<PostSaveResponse> getMySavedPosts(int page, int size);
    PageResponse<PostSaveResponse> getAllSavedPostsAdmin(int page, int size);
}
