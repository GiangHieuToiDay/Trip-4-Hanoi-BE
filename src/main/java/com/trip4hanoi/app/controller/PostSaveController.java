package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.dto.res.PostSaveResponse;
import com.trip4hanoi.app.service.PostSaveService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/post-saves")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@CrossOrigin(origins = "*")
public class PostSaveController {

    PostSaveService postSaveService;

    @PostMapping("/{postId}")
    public ResponseEntity<APIResponse<PostSaveResponse>> savePost(@PathVariable Long postId) {
        PostSaveResponse response = postSaveService.savePost(postId);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                APIResponse.<PostSaveResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .code(1000)
                        .message("Post saved successfully")
                        .data(response)
                        .build()
        );
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<APIResponse<Void>> unsavePost(@PathVariable Long postId) {
        postSaveService.unsavePost(postId);
        return ResponseEntity.ok(
                APIResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .code(1000)
                        .message("Post unsaved successfully")
                        .build()
        );
    }

    @GetMapping("/my")
    public ResponseEntity<APIResponse<PageResponse<PostSaveResponse>>> getMySavedPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<PostSaveResponse> response = postSaveService.getMySavedPosts(page, size);
        return ResponseEntity.ok(
                APIResponse.<PageResponse<PostSaveResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .code(1000)
                        .message("My saved posts retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/admin/all")
    public ResponseEntity<APIResponse<PageResponse<PostSaveResponse>>> getAllSavedPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<PostSaveResponse> response = postSaveService.getAllSavedPosts(page, size);
        return ResponseEntity.ok(
                APIResponse.<PageResponse<PostSaveResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .code(1000)
                        .message("All saved posts retrieved successfully")
                        .data(response)
                        .build()
        );
    }
}
