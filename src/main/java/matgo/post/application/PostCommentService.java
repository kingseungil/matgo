package matgo.post.application;

import static matgo.global.exception.ErrorCode.NOT_FOUND_MEMBER;
import static matgo.global.exception.ErrorCode.NOT_FOUND_POST;
import static matgo.global.exception.ErrorCode.NOT_FOUND_POST_COMMENT;
import static matgo.global.exception.ErrorCode.NOT_OWNER_POST_COMMENT;

import lombok.RequiredArgsConstructor;
import matgo.global.lock.annotation.DistributedLock;
import matgo.member.domain.entity.Member;
import matgo.member.domain.repository.MemberRepository;
import matgo.member.exception.MemberException;
import matgo.post.domain.entity.Post;
import matgo.post.domain.entity.PostComment;
import matgo.post.domain.repository.PostCommentQueryRepository;
import matgo.post.domain.repository.PostCommentRepository;
import matgo.post.domain.repository.PostRepository;
import matgo.post.dto.request.PostCommentCreateRequest;
import matgo.post.dto.response.PostCommentSliceResponse;
import matgo.post.exception.PostException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostCommentService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostCommentQueryRepository postCommentQueryRepository;

    @DistributedLock(key = "'createComment-' + #postId")
    public void createComment(Long memberId, Long postId, PostCommentCreateRequest postCommentCreateRequest) {
        Post post = getPostById(postId);
        Member member = getMemberById(memberId);

        PostComment postComment = PostCommentCreateRequest.toEntity(post, member, postCommentCreateRequest);
        post.addPostComment(postComment);
        member.addPostComment(postComment);

        postCommentRepository.save(postComment);
    }

    private Post getPostById(Long postId) {
        return postRepository.findById(postId)
                             .orElseThrow(() -> new PostException(NOT_FOUND_POST));
    }

    @Transactional
    public void updateComment(Long memberId, Long commentId,
      PostCommentCreateRequest postCommentCreateRequest) {
        Member member = getMemberById(memberId);
        PostComment postComment = getPostCommentById(commentId);
        checkPostCommentOwner(member, postComment);

        updatePostCommentIfChanged(postComment, postCommentCreateRequest);
    }

    private void checkPostCommentOwner(Member member, PostComment postComment) {
        if (!postComment.getMember().equals(member)) {
            throw new PostException(NOT_OWNER_POST_COMMENT);
        }
    }

    private void updatePostCommentIfChanged(PostComment postComment, PostCommentCreateRequest request) {
        if (request.content() != null && !request.content().isEmpty()) {
            postComment.updateContent(request.content());
        }
    }

    @DistributedLock(key = "'deleteComment-' + #postId")
    public void deleteComment(Long memberId, Long commentId) {
        Member member = getMemberById(memberId);
        PostComment postComment = getPostCommentById(commentId);
        Post post = postComment.getPost();
        checkPostCommentOwner(member, postComment);

        member.removePostComment(postComment);
        post.removePostComment(postComment);
        postCommentRepository.delete(postComment);
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                               .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
    }

    private PostComment getPostCommentById(Long commentId) {
        return postCommentRepository.findById(commentId)
                                    .orElseThrow(() -> new PostException(NOT_FOUND_POST_COMMENT));
    }

    @Transactional(readOnly = true)
    public PostCommentSliceResponse getMyComments(Long memberId, Pageable pageable) {
        return postCommentQueryRepository.findAllByMemberId(memberId, pageable);
    }
}
