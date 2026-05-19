package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.common.PostStatus;
import com.trip4hanoi.app.dto.req.PostRequest;
import com.trip4hanoi.app.dto.res.PostResponse;
import com.trip4hanoi.app.entity.Place;
import com.trip4hanoi.app.entity.Post;
import com.trip4hanoi.app.entity.PostImage;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.mapper.PostMapper;
import com.trip4hanoi.app.repository.PlaceRepository;
import com.trip4hanoi.app.repository.PostRepository;
import com.trip4hanoi.app.repository.UserRepository;
import com.trip4hanoi.app.service.CloudinaryService;
import com.trip4hanoi.app.service.PostService;
import com.trip4hanoi.app.service.SmartNotificationEngine;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
@FieldDefaults( level = AccessLevel.PRIVATE, makeFinal = true)
public class PostServiceImpl implements PostService {

    PostRepository postRepository;
    PostMapper postMapper;
    UserRepository userRepository;
    PlaceRepository placeRepository;
    CloudinaryService cloudinaryService;
    SmartNotificationEngine smartNotificationEngine;


    @Override
    public Page<PostResponse> findAllPost(int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        // PUBLIC VIEW: Only show APPROVED posts
        Page<Post> postPage = postRepository.findByStatus(PostStatus.APPROVED, pageable);
        return postPage.map(postMapper::toPostResponse);
    }

    @Override
    public Page<PostResponse> findAllPostAdmin(String keyword, PostStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").descending()
        );

        Page<Post> postPage;
        if (org.springframework.util.StringUtils.hasText(keyword)) {
            if (status != null) {
                postPage = postRepository.findByTitleContainingIgnoreCaseAndStatus(keyword, status, pageable);
            } else {
                postPage = postRepository.findByTitleContainingIgnoreCase(keyword, pageable);
            }
        } else if (status != null) {
            postPage = postRepository.findByStatus(status, pageable);
        } else {
            postPage = postRepository.findAll(pageable);
        }

        return postPage.map(postMapper::toPostResponse);
    }

    @Override
    public PostResponse findPostById(long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        return postMapper.toPostResponse(post);
    }

    @Override
    public Page<PostResponse> findAllPostByTitle(int page, int size, String title) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );
        // PUBLIC SEARCH: Only show APPROVED posts
        Page<Post> pagePost = postRepository.findByTitleContainingIgnoreCaseAndStatus(title, PostStatus.APPROVED, pageable);
        return pagePost.map(postMapper::toPostResponse);
    }

    @Override
    public PostResponse createPost(PostRequest request, List<MultipartFile> images) {
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();
        Long userId = 0L;
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            userId = (Long) jwt.getClaims().get("id");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(user)
                .status(PostStatus.PENDING) // Explicitly set as PENDING
                .images(new ArrayList<>())
                .build();

        // Upload images
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                if (image != null && !image.isEmpty()) {
                    try {
                        Map uploadResult = cloudinaryService.uploadFile(image);
                        PostImage postImage = PostImage.builder()
                                .imageUrl(uploadResult.get("secure_url").toString())
                                .publicId(uploadResult.get("public_id").toString())
                                .post(post)
                                .build();
                        post.getImages().add(postImage);
                    } catch (Exception e) {
                        log.error("Upload image failed: {}", e.getMessage());
                        throw new AppException(ErrorCode.UPLOAD_FAIL);
                    }
                }
            }
        }

        // Tag places
        if (request.getTaggedPlaceIds() != null && !request.getTaggedPlaceIds().isEmpty()) {
            List<Place> places = placeRepository.findAllById(request.getTaggedPlaceIds());
            post.setPlaces(new ArrayList<>(places));
        }

        Post savedPost = postRepository.save(post);
        
        // --- SMART NOTIFICATION TRIGGER ---
        // Thông báo cho Admin biết có bài viết mới cần duyệt
        smartNotificationEngine.notifyAdminNewPost(savedPost);

        return postMapper.toPostResponse(savedPost);
    }

    @Override
    public PostResponse updatePost(long id, PostRequest request, List<MultipartFile> images) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        // Update places
        if (request.getTaggedPlaceIds() != null) {
            List<Place> places = placeRepository.findAllById(request.getTaggedPlaceIds());
            post.setPlaces(new ArrayList<>(places));
        }

        // Update images
        List<PostImage> currentImages = post.getImages();

        // 1. Remove images not in keepImageIds
        if (request.getKeepImageIds() != null) {
            List<PostImage> toRemove = new ArrayList<>();
            for (PostImage img : currentImages) {
                if (!request.getKeepImageIds().contains(img.getId())) {
                    toRemove.add(img);
                }
            }

            for (PostImage img : toRemove) {
                if (img.getPublicId() != null) {
                    try {
                        cloudinaryService.deleteFile(img.getPublicId());
                    } catch (Exception e) {
                        log.error("Delete cloudinary failed for publicId: {}", img.getPublicId());
                    }
                }
            }
            currentImages.removeAll(toRemove);
        }

        // 2. Add new images
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                if (image != null && !image.isEmpty()) {
                    try {
                        Map uploadResult = cloudinaryService.uploadFile(image);
                        PostImage newImage = PostImage.builder()
                                .imageUrl(uploadResult.get("secure_url").toString())
                                .publicId(uploadResult.get("public_id").toString())
                                .post(post)
                                .build();
                        currentImages.add(newImage);
                    } catch (Exception e) {
                        log.error("Upload image failed: {}", e.getMessage());
                        throw new AppException(ErrorCode.UPLOAD_FAIL);
                    }
                }
            }
        }

        return postMapper.toPostResponse(postRepository.save(post));
    }

    @Override
    public void deletePost(long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post != null && post.getImages() != null) {
            for (PostImage img : post.getImages()) {
                if (img.getPublicId() != null) {
                    cloudinaryService.deleteFile(img.getPublicId());
                }
            }
        }
        postRepository.deleteById(id);
    }

    @Override
    public void updatePostStatus(Long id, PostStatus status) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        post.setStatus(status);
        postRepository.save(post);
        
        // Gửi thông báo cho tác giả bài viết
        smartNotificationEngine.notifyUserPostStatus(post);
    }

    @Override
    public List<PostResponse> findTop5PostsByUpvotes() {
        // Implement logic for top posts if needed
        return new ArrayList<>();
    }

    @Override
    public List<PostResponse> getPostByUser() {
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();
        Long userId = 0L;
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            userId = (Long) jwt.getClaims().get("id");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        List<Post> posts = postRepository.findPostByUser(user);

        return posts.stream().map(postMapper::toPostResponse).toList();
    }
}