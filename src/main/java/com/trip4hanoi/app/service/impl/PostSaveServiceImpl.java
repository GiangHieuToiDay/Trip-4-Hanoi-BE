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
public class PostSaveServiceImpl implements PostSaveService {

    PostSaveRepository postSaveRepository;
    PostRepository postRepository;
    UserRepository userRepository;
    PostSaveMapper postSaveMapper;

    @Override
    public PostSaveResponse savePost(Long postId) {
        // TODO: Get userId from SecurityContext
        long userId = 1;

        if (postSaveRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new AppException(ErrorCode.POST_ALREADY_SAVED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId.longValue());
        if (post == null) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }

        PostSave postSave = PostSave.builder()
                .post(post)
                .user(user)
                .build();

        return postSaveMapper.toPostSaveResponse(postSaveRepository.save(postSave));
    }

    @Override
    public void unsavePost(Long postId) {
        // TODO: Get userId from SecurityContext
        long userId = 1;

        PostSave postSave = postSaveRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_SAVED));

        postSaveRepository.delete(postSave);
    }

    @Override
    public PageResponse<PostSaveResponse> getMySavedPosts(int page, int size) {
        // TODO: Get userId from SecurityContext
        long userId = 1;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<PostSave> postSavePage = postSaveRepository.findByUserId(userId, pageable);

        List<PostSaveResponse> responses = postSavePage.getContent().stream()
                .map(postSaveMapper::toPostSaveResponse)
                .collect(Collectors.toList());

        return PageResponse.from(postSavePage, responses);
    }

    @Override
    public PageResponse<PostSaveResponse> getAllSavedPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<PostSave> postSavePage = postSaveRepository.findAll(pageable);

        List<PostSaveResponse> responses = postSavePage.getContent().stream()
                .map(postSaveMapper::toPostSaveResponse)
                .collect(Collectors.toList());

        return PageResponse.from(postSavePage, responses);
    }
}
