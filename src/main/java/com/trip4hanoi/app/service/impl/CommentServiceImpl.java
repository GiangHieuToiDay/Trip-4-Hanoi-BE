package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.req.CommentRequest;
import com.trip4hanoi.app.dto.res.CommentResponse;
import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.entity.Comment;
import com.trip4hanoi.app.entity.Post;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.mapper.CommentMapper;
import com.trip4hanoi.app.repository.CommentRepository;
import com.trip4hanoi.app.repository.PostRepository;
import com.trip4hanoi.app.repository.UserRepository;
import com.trip4hanoi.app.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "COMMENT-SERVICE")
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    private Long getCurrentUserId() {
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return (Long) jwt.getClaims().get("id");
        }
        return 0L;
    }

    @Override
    @Transactional
    public CommentResponse createComment(CommentRequest request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(request.getPostId().longValue())
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .user(user)
                .post(post)
                .build();

        return commentMapper.toCommentResponse(commentRepository.save(comment));
    }

    @Override
    public CommentResponse updateComment(Long id, CommentRequest request) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        Long userId = getCurrentUserId();
        if (!comment.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.COMMENT_NOT_BY_USER);
        }

        comment.setContent(request.getContent());
        return commentMapper.toCommentResponse(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        Long userId = getCurrentUserId();
        // Allow deletion if it's the owner or MANAGE_USER (admin)
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("MANAGE_USER"));

        if (!comment.getUser().getId().equals(userId) && !isAdmin) {
            throw new AppException(ErrorCode.COMMENT_NOT_BY_USER);
        }

        commentRepository.delete(comment);
    }



//    @Override
//    public CommentResponse getCommentById(Long id) {
//        return commentRepository.findById(id)
//                .map(commentMapper::toCommentResponse)
//                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
//    }

    @Override
    public PageResponse<CommentResponse> getCommentsByPost(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Comment> commentPage = commentRepository.findByPostId(postId, pageable);

        List<CommentResponse> data = commentPage.getContent().stream()
                .map(commentMapper::toCommentResponse)
                .collect(Collectors.toList());

        return PageResponse.from(commentPage, data);
    }

    @Override
    public PageResponse<CommentResponse> getAllComments(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
        Page<Comment> commentPage = commentRepository.findAll(pageable);

        List<CommentResponse> data = commentPage.getContent().stream()
                .map(commentMapper::toCommentResponse)
                .collect(Collectors.toList());

        return PageResponse.from(commentPage, data);
    }
}
