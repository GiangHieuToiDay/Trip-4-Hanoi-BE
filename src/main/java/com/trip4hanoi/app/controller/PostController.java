package com.trip4hanoi.app.controller;


import com.trip4hanoi.app.dto.req.PostRequest;
import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.PostResponse;
import com.trip4hanoi.app.service.PostLikeService;
import com.trip4hanoi.app.service.PostService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@FieldDefaults( level = AccessLevel.PRIVATE, makeFinal = true)
public class PostController {

    PostService postService;
    PostLikeService postLikeService;

    @PostMapping("/{id}/like")
    public ResponseEntity<APIResponse<Void>> toggleLike(@PathVariable Long id) {
        postLikeService.toggleLike(id);
        return ResponseEntity.ok(
                APIResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .code(1000)
                        .message("Toggle like successfully")
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<APIResponse<Page<PostResponse>>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<PostResponse> posts = postService.findAllPost(page, size);

        return ResponseEntity.ok(
                APIResponse.<Page<PostResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .code(1000)
                        .message("Successfully retrieved posts")
                        .data(posts)
                        .build()
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<PostResponse>> getPostById(@PathVariable long id) {

        PostResponse post = postService.findPostById(id);

        return ResponseEntity.ok(
                APIResponse.<PostResponse>builder()
                        .status(HttpStatus.OK.value())
                        .code(1000)
                        .message("Successfully retrieved post")
                        .data(post)
                        .build()
        );
    }


    @GetMapping("/search")
    public ResponseEntity<APIResponse<Page<PostResponse>>> searchPosts(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<PostResponse> posts = postService.findAllPostByTitle(page, size, title);

        return ResponseEntity.ok(
                APIResponse.<Page<PostResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .code(1000)
                        .message("Search posts successfully")
                        .data(posts)
                        .build()
        );
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<APIResponse<PostResponse>> createPost(
            @Valid @RequestPart("data") PostRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        PostResponse post = postService.createPost(request, images);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                APIResponse.<PostResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .code(1000)
                        .message("Post created successfully")
                        .data(post)
                        .build()
        );
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<APIResponse<PostResponse>> updatePost(
            @PathVariable long id,
            @RequestPart("data") @Valid PostRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        PostResponse post = postService.updatePost(id, request, images);

        return ResponseEntity.ok(
                APIResponse.<PostResponse>builder()
                        .status(HttpStatus.OK.value())
                        .code(1000)
                        .message("Post updated successfully")
                        .data(post)
                        .build()
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> deletePost(@PathVariable long id) {

        postService.deletePost(id);

        return ResponseEntity.ok(
                APIResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .code(1000)
                        .message("Post deleted successfully")
                        .build()
        );
    }


    @GetMapping("/top")
    public ResponseEntity<APIResponse<List<PostResponse>>> getTopPosts() {

        List<PostResponse> posts = postService.findTop5PostsByUpvotes();

        return ResponseEntity.ok(
                APIResponse.<List<PostResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .code(1000)
                        .message("Top posts retrieved successfully")
                        .data(posts)
                        .build()
        );
    }

    @GetMapping("/me")
    public ResponseEntity<APIResponse<List<PostResponse>>> getMyPosts() {

        List<PostResponse> posts = postService.getPostByUser();

        return ResponseEntity.ok(
                APIResponse.<List<PostResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .code(1000)
                        .message("User posts retrieved successfully")
                        .data(posts)
                        .build()
        );
    }
}
