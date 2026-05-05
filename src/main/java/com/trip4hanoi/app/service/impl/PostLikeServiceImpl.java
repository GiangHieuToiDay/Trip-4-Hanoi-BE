package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.entity.Post;
import com.trip4hanoi.app.entity.PostLike;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.repository.PostLikeRepository;
import com.trip4hanoi.app.repository.PostRepository;
import com.trip4hanoi.app.repository.UserRepository;
import com.trip4hanoi.app.service.PostLikeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class PostLikeServiceImpl implements PostLikeService {

    PostLikeRepository postLikeRepository;
    PostRepository postRepository;
    UserRepository userRepository;

    @Override
    public void toggleLike(Long postId) {
        // TODO: Get userId from SecurityContext
        long userId = 1;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(postId.longValue());
        if (post == null) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }

        Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, user);

        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
        } else {
            PostLike newLike = PostLike.builder()
                    .post(post)
                    .user(user)
                    .build();
            postLikeRepository.save(newLike);
        }
    }

    @Override
    public long countLikes(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    @Override
    public boolean isLikedByUser(Long postId, Long userId) {
        Post post = postRepository.findById(postId.longValue());
        if (post == null) return false;
        User user = User.builder().id(userId).build();
        return postLikeRepository.existsByPostAndUser(post, user);
    }
}
