package com.trip4hanoi.app.service.impl;

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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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


    @Override
    public Page<PostResponse> findAllPost(int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<Post> postPage = postRepository.findAll(pageable);

        if (postPage.isEmpty()) {
            throw new AppException(ErrorCode.POST_IS_EMPTY);
        }

        return postPage.map(postMapper::toPostResponse);
    }

    @Override
    public PostResponse findPostById(long id) {
        Post post = postRepository.findById(id);
        if (post == null) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }
        return postMapper.toPostResponse(post);
    }

    @Override
    public Page<PostResponse> findAllPostByTitle(int page, int size, String title) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );
        Page<Post> pagePost = postRepository.findByTitleContainingIgnoreCase(title, pageable);
        if (pagePost.isEmpty()) {
            throw new AppException(ErrorCode.POST_IS_EMPTY);
        }
        return pagePost.map(postMapper::toPostResponse);
    }

    @Override
    public PostResponse createPost(PostRequest request, List<MultipartFile> images) {
        // TODO: Get userId from SecurityContext
        long userId = 1;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(user)
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

        return postMapper.toPostResponse(postRepository.save(post));
    }

    @Override
    public PostResponse updatePost(long id, PostRequest request, List<MultipartFile> images) {
        Post post = postRepository.findById(id);
        if (post == null) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }

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
        Post post = postRepository.findById(id);
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
    public List<PostResponse> findTop5PostsByUpvotes() {
        // Implement logic for top posts if needed
        return new ArrayList<>();
    }

    @Override
    public List<PostResponse> getPostByUser() {
        // TODO: Get userId from SecurityContext
        long userId = 1;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        List<Post> posts = postRepository.findPostByUser(user);
        if (posts.isEmpty()) {
            throw new AppException(ErrorCode.POST_IS_EMPTY);
        }
        return posts.stream().map(postMapper::toPostResponse).toList();
    }
}
