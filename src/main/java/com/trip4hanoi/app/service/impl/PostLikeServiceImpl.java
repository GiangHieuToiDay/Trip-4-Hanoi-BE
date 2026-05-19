package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.dto.res.PostLikeResponse;
import com.trip4hanoi.app.entity.Post;
import com.trip4hanoi.app.entity.PostLike;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.mapper.PostLikeMapper;
import com.trip4hanoi.app.repository.PostLikeRepository;
import com.trip4hanoi.app.repository.PostRepository;
import com.trip4hanoi.app.repository.UserRepository;
import com.trip4hanoi.app.service.PostLikeService;
import com.trip4hanoi.app.service.SmartNotificationEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "POST-LIKE-SERVICE")
public class PostLikeServiceImpl implements PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeMapper postLikeMapper;
    private final SmartNotificationEngine smartNotificationEngine;

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
    public void toggleLike(Long postId) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        postLikeRepository.findByUserAndPost(user, post).ifPresentOrElse(
                postLikeRepository::delete,
                () -> {
                    postLikeRepository.save(PostLike.builder().user(user).post(post).build());
                    // Gửi thông báo khi có Like mới
                    smartNotificationEngine.notifyUserPostLike(user, post);
                }
        );
    }

    @Override
    public List<PostLikeResponse> getLikesByPostId(Long postId) {
        return postLikeRepository.findByPostId(postId).stream()
                .map(postLikeMapper::toPostLikeResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long countLikesByPostId(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    @Override
    public boolean isPostLikedByUser(Long postId, Long userId) {
        return postLikeRepository.existsByPostIdAndUserId(postId, userId);
    }
}
