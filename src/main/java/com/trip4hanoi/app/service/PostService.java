package com.trip4hanoi.app.service;


import com.trip4hanoi.app.common.PostStatus;
import com.trip4hanoi.app.dto.req.PostRequest;
import com.trip4hanoi.app.dto.res.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface PostService {

    Page<PostResponse> findAllPost(int page, int size);

    Page<PostResponse> findAllPostAdmin(String keyword, PostStatus status, int page, int size);

    PostResponse findPostById(long id);

    Page<PostResponse> findAllPostByTitle(int page, int size, String title);

    PostResponse createPost(PostRequest postDtoRequest, List<MultipartFile> images);

    PostResponse updatePost(long id, PostRequest request, List<MultipartFile> images);

    void deletePost(long id);

    void updatePostStatus(Long id, PostStatus status);

    List<PostResponse> findTop5PostsByUpvotes();

    List<PostResponse> getPostByUser();

}
