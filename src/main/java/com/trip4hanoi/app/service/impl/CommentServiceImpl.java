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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class CommentServiceImpl implements CommentService {

    CommentRepository commentRepository;
    CommentMapper commentMapper;
    PostRepository postRepository;
    UserRepository userRepository;

    @Override
    public CommentResponse createComment(CommentRequest request) {
        // TODO: Get userId from SecurityContext
        long userId = 1;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(request.getPostId().longValue());
        if (post == null) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }

        Comment comment = commentMapper.toComment(request);
        comment.setUser(user);
        comment.setPost(post);

        return commentMapper.toCommentResponse(commentRepository.save(comment));
    }

    @Override
    public CommentResponse updateComment(Long commentId, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        // TODO: Check if user is the owner of the comment
        
        comment.setContent(content);
        return commentMapper.toCommentResponse(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        // TODO: Check if user is the owner of the comment
        
        commentRepository.delete(comment);
    }

    @Override
    public PageResponse<CommentResponse> getCommentsByPost(Long postId, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Comment> commentPage = commentRepository.findByPostId(postId, pageable);

        List<CommentResponse> responses = commentPage.getContent().stream()
                .map(commentMapper::toCommentResponse)
                .collect(Collectors.toList());

        return PageResponse.from(commentPage, responses);
    }

    @Override
    public PageResponse<CommentResponse> getAllComments(int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Comment> commentPage = commentRepository.findAll(pageable);

        List<CommentResponse> responses = commentPage.getContent().stream()
                .map(commentMapper::toCommentResponse)
                .collect(Collectors.toList());

        return PageResponse.from(commentPage, responses);
    }
}
