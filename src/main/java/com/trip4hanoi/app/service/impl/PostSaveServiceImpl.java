package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.res.PageResponse;
import com.trip4hanoi.app.dto.res.PostSaveResponse;
import com.trip4hanoi.app.entity.Post;
import com.trip4hanoi.app.entity.PostSave;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.mapper.PostSaveMapper;
import com.trip4hanoi.app.repository.PostRepository;
import com.trip4hanoi.app.repository.PostSaveRepository;
import com.trip4hanoi.app.repository.UserRepository;
import com.trip4hanoi.app.service.PostSaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "POST-SAVE-SERVICE")
public class PostSaveServiceImpl implements PostSaveService {

    private final PostSaveRepository postSaveRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostSaveMapper postSaveMapper;

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
    public PostSaveResponse savePost(Long postId) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        if (postSaveRepository.existsByUserAndPost(user, post)) {
            throw new AppException(ErrorCode.POST_ALREADY_SAVED);
        }

        PostSave postSave = PostSave.builder()
                .user(user)
                .post(post)
                .build();

        postSaveRepository.save(postSave);
        return postSaveMapper.toPostSaveResponse(postSaveRepository.save(postSave));

    }

    @Override
    @Transactional
    public void unsavePost(Long postId) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        PostSave postSave = postSaveRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_SAVED));

        postSaveRepository.delete(postSave);
    }

    @Override
    public PageResponse<PostSaveResponse> getMySavedPosts(int page, int size) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<PostSave> postSavePage = postSaveRepository.findByUserId(userId, pageable);
        List<PostSaveResponse> responses = postSavePage.getContent().stream()
                .map(postSaveMapper::toPostSaveResponse)
                .collect(Collectors.toList());

        return PageResponse.from(postSavePage, responses);
    }

    @Override
    public PageResponse<PostSaveResponse> getAllSavedPostsAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<PostSave> postSavePage = postSaveRepository.findAll(pageable);

        List<PostSaveResponse> data = postSavePage.getContent().stream()
                .map(postSaveMapper::toPostSaveResponse)
                .collect(Collectors.toList());

        return PageResponse.from(postSavePage, data);
    }
}
