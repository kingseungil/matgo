package matgo.post.application;

import static matgo.global.exception.ErrorCode.IMAGES_SIZE_EXCEED;
import static matgo.global.exception.ErrorCode.NOT_FOUND_MEMBER;
import static matgo.global.exception.ErrorCode.NOT_FOUND_POST;
import static matgo.global.exception.ErrorCode.NOT_FOUND_POST_REACTION;
import static matgo.global.exception.ErrorCode.NOT_OWNER_POST;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matgo.global.filesystem.s3.S3Service;
import matgo.global.lock.annotation.DistributedLock;
import matgo.global.type.Reaction;
import matgo.global.type.S3Directory;
import matgo.member.domain.entity.Member;
import matgo.member.domain.repository.MemberRepository;
import matgo.member.exception.MemberException;
import matgo.post.domain.entity.Post;
import matgo.post.domain.entity.PostImage;
import matgo.post.domain.entity.PostReaction;
import matgo.post.domain.repository.PostImageRepository;
import matgo.post.domain.repository.PostQueryRepository;
import matgo.post.domain.repository.PostReactionRepository;
import matgo.post.domain.repository.PostRepository;
import matgo.post.dto.request.PostCreateRequest;
import matgo.post.dto.request.PostUpdateRequest;
import matgo.post.dto.response.PostCreateResponse;
import matgo.post.dto.response.PostDetailResponse;
import matgo.post.dto.response.PostSliceResponse;
import matgo.post.exception.PostException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PostQueryRepository postQueryRepository;
    private final PostImageRepository postImageRepository;
    private final PostReactionRepository postReactionRepository;
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

        updatePostImagesIfPresent(postImages, post);
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

    private void updatePostImagesIfPresent(List<MultipartFile> postImages, Post post) {
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

    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetailByRegion(Long memberId, Long postId) {
        Member member = getMemberById(memberId);
        return postQueryRepository.findPostDetailResponseById(member.getRegion().getId(), postId)
                                  .orElseThrow(() -> new PostException(NOT_FOUND_POST));
    }

    @Transactional(readOnly = true)
    public PostSliceResponse getPostsByRegion(Long memberId, Pageable pageable) {
        Member member = getMemberById(memberId);
        return postQueryRepository.findAllPostSliceByRegionId(member.getRegion().getId(), pageable);
    }

    @DistributedLock(key = "'addPostReaction-' + #postId")
    public void addPostReaction(Long memberId, Long postId, Reaction reactionType) {
        Post post = getPostById(postId);
        Member member = getMemberById(memberId);

        // 이미 반응이 있다면 기존 반응 삭제 후 새로운 반응으로 업데이트
        if (post.hasReaction(member)) {
            updateReaction(post, member, reactionType);
        } else {
            addReaction(post, member, reactionType);
        }

        postRepository.save(post);
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                               .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
    }

    private void updateReaction(Post post, Member member, Reaction reaction) {
        PostReaction postReaction = post.getPostReactions()
                                        .stream()
                                        .filter(r -> r.getMember().getId().equals(member.getId()))
                                        .findFirst()
                                        .orElseThrow(
                                          () -> new PostException(NOT_FOUND_POST_REACTION));

        // 기존 반응과 같다면 반응 삭제 (아무것도 안누른 상태로 변경)
        if (postReaction.getReaction() == reaction) {
            postReactionRepository.delete(postReaction);
            post.removePostReaction(postReaction);
            if (reaction == Reaction.LIKE) {
                post.decreaseLikeCount();
            } else {
                post.decreaseDislikeCount();
            }
        } else { // 기존 반응과 다르다면 반응 업데이트
            postReaction.changeReaction(reaction);
            if (reaction == Reaction.LIKE) {
                post.increaseLikeCount();
                post.decreaseDislikeCount();
            } else {
                post.increaseDislikeCount();
                post.decreaseLikeCount();
            }
        }
    }

    private void addReaction(Post post, Member member, Reaction reaction) {
        PostReaction postReaction = PostReaction.from(post, member, reaction);
        post.addPostReaction(postReaction);
        postReactionRepository.save(postReaction);
        if (reaction == Reaction.LIKE) {
            post.increaseLikeCount();
        } else {
            post.increaseDislikeCount();
        }
    }
}
