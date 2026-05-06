package com.trip4hanoi.app.controller;

import com.trip4hanoi.app.dto.req.CommentRequest;
import com.trip4hanoi.app.dto.res.APIResponse;
import com.trip4hanoi.app.dto.res.CommentResponse;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.service.CommentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@CrossOrigin(origins = "*")
public class CommentController {

    CommentService commentService;

    @PostMapping
    public ResponseEntity<APIResponse<CommentResponse>> createComment(@Valid @RequestBody CommentRequest request) {
        CommentResponse comment = commentService.createComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                APIResponse.<CommentResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .code(1000)
                        .message("Comment created successfully")
                        .data(comment)
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<CommentResponse>> updateComment(
            @PathVariable Long id,
            @RequestBody String content) {
        CommentResponse comment = commentService.updateComment(id, content);
        return ResponseEntity.ok(
                APIResponse.<CommentResponse>builder()
                        .status(HttpStatus.OK.value())
                        .code(1000)
                        .message("Comment updated successfully")
                        .data(comment)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok(
                APIResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .code(1000)
                        .message("Comment deleted successfully")
                        .build()
        );
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<APIResponse<PageResponse<CommentResponse>>> getCommentsByPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<CommentResponse> comments = commentService.getCommentsByPost(postId, page, size);
        return ResponseEntity.ok(
                APIResponse.<PageResponse<CommentResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .code(1000)
                        .message("Comments retrieved successfully")
                        .data(comments)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<APIResponse<PageResponse<CommentResponse>>> getAllComments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<CommentResponse> comments = commentService.getAllComments(page, size);
        return ResponseEntity.ok(
                APIResponse.<PageResponse<CommentResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .code(1000)
                        .message("All comments retrieved successfully")
                        .data(comments)
                        .build()
        );
    }
}
