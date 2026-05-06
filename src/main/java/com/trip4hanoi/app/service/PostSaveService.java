package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.req.SavedPlaceRequest;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.dto.res.PostSaveResponse;

public interface PostSaveService {
    PostSaveResponse savePost(Long postId);
    void unsavePost(Long postId);
    PageResponse<PostSaveResponse> getMySavedPosts(int page, int size);
    PageResponse<PostSaveResponse> getAllSavedPosts(int page, int size);
}
