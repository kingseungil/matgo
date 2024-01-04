package matgo.post.application;

import static matgo.global.exception.ErrorCode.IMAGES_SIZE_EXCEED;
import static matgo.global.exception.ErrorCode.NOT_FOUND_MEMBER;
import static matgo.global.exception.ErrorCode.NOT_FOUND_POST;
import static matgo.global.exception.ErrorCode.NOT_OWNER_POST;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matgo.global.filesystem.s3.S3Service;
import matgo.global.type.S3Directory;
import matgo.member.domain.entity.Member;
import matgo.member.domain.repository.MemberRepository;
import matgo.member.exception.MemberException;
import matgo.post.domain.entity.Post;
import matgo.post.domain.entity.PostImage;
import matgo.post.domain.repository.PostImageRepository;
import matgo.post.domain.repository.PostRepository;
import matgo.post.dto.request.PostCreateRequest;
import matgo.post.dto.request.PostUpdateRequest;
import matgo.post.dto.response.PostCreateResponse;
import matgo.post.exception.PostException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final S3Service s3Service;

    @Transactional
    public PostCreateResponse createPost(Long memberId, PostCreateRequest postCreateRequest,
      List<MultipartFile> postImages) {
        checkImageSize(postImages);
        Member member = getMemberById(memberId);

        Post post = PostCreateRequest.toEntity(member, postCreateRequest);
        List<PostImage> postImageList = uploadPostImages(postImages, post);
        savePostImages(postImageList, post);
        postRepository.save(post);

        return new PostCreateResponse(post.getId());
    }

    private void checkImageSize(List<MultipartFile> postImages) {
        if (postImages != null && postImages.size() > 3) {
            throw new PostException(IMAGES_SIZE_EXCEED);
        }
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                               .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
    }

    private List<PostImage> uploadPostImages(List<MultipartFile> postImages, Post post) {
        List<PostImage> postImageList = new ArrayList<>();
        if (postImages != null && !postImages.isEmpty()) {
            for (MultipartFile postImage : postImages) {
                String imageUrl = s3Service.uploadAndGetImageURL(postImage, S3Directory.POST);
                PostImage postImageEntity = PostImage.builder().post(post).imageUrl(imageUrl).build();
                postImageList.add(postImageEntity);
            }
        }
        return postImageList;
    }

    private void savePostImages(List<PostImage> postImages, Post post) {
        post.addPostImages(postImages);
    }

    @Transactional
    public void updatePost(Long postId, Long memberId, PostUpdateRequest postUpdateRequest,
      List<MultipartFile> postImages) {
        checkImageSize(postImages);

        Member member = getMemberById(memberId);
        Post post = getPostById(postId);
        checkPostOwner(member, post);

        updatePostImagesIfPresent(postImages, member, post);
        updatePostIfChanged(postUpdateRequest, post);
    }

    private Post getPostById(Long postId) {
        return postRepository.findById(postId)
                             .orElseThrow(() -> new PostException(NOT_FOUND_POST));
    }

    private void checkPostOwner(Member member, Post post) {
        if (!post.getMember().equals(member)) {
            throw new PostException(NOT_OWNER_POST);
        }
    }

    private void updatePostImagesIfPresent(List<MultipartFile> postImages, Member member, Post post) {
        if (postImages != null && !postImages.isEmpty()) {
            // 기존 이미지 삭제
            deletePostImages(post);
            // 새로운 이미지 업로드
            List<PostImage> postImageList = uploadPostImages(postImages, post);
            savePostImages(postImageList, post);
        }
    }

    private void deletePostImages(Post post) {
        List<PostImage> postImages = postImageRepository.findAllByPost(post);
        for (PostImage postImage : postImages) {
            s3Service.delete(postImage.getImageUrl());
        }
        postImageRepository.deleteAll(postImages);
    }

    private void updatePostIfChanged(PostUpdateRequest postUpdateRequest, Post post) {
        if (postUpdateRequest.title() != null && !postUpdateRequest.title().isEmpty()) {
            post.updateTitle(postUpdateRequest.title());
        }
        if (postUpdateRequest.content() != null && !postUpdateRequest.content().isEmpty()) {
            post.updateContent(postUpdateRequest.content());
        }
    }

    @Transactional
    public void deletePost(Long postId, Long memberId) {
        Member member = getMemberById(memberId);
        Post post = getPostById(postId);
        checkPostOwner(member, post);

        deletePostImages(post);
        postRepository.delete(post);
    }
}
