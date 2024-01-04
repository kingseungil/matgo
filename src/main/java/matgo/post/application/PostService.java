package matgo.post.application;

import static matgo.global.exception.ErrorCode.IMAGES_SIZE_EXCEED;
import static matgo.global.exception.ErrorCode.NOT_FOUND_MEMBER;

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
import matgo.post.domain.repository.PostRepository;
import matgo.post.dto.request.PostCreateRequest;
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
    private final S3Service s3Service;

    @Transactional
    public PostCreateResponse createPost(Long memberId, PostCreateRequest postCreateRequest,
      List<MultipartFile> postImages) {
        checkImageSize(postImages);

        Member member = memberRepository.findById(memberId)
                                        .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));

        Post post = PostCreateRequest.toEntity(member, postCreateRequest);
        List<PostImage> postImageList = uploadPostImages(postImages, post);
        savePostImages(postImageList, post);
        postRepository.save(post);

        return new PostCreateResponse(post.getId());
    }

    private void checkImageSize(List<MultipartFile> postImages) {
        if (postImages.size() > 3) {
            throw new PostException(IMAGES_SIZE_EXCEED);
        }
    }

    private List<PostImage> uploadPostImages(List<MultipartFile> postImages, Post post) {
        List<PostImage> postImageList = new ArrayList<>();
        for (MultipartFile postImage : postImages) {
            String imageUrl = s3Service.uploadAndGetImageURL(postImage, S3Directory.POST);
            PostImage postImageEntity = PostImage.builder().post(post).imageUrl(imageUrl).build();
            postImageList.add(postImageEntity);
        }
        return postImageList;
    }

    private void savePostImages(List<PostImage> postImages, Post post) {
        post.addPostImages(postImages);
    }

}
