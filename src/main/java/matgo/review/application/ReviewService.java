package matgo.review.application;

import static matgo.global.exception.ErrorCode.ALREADY_WRITTEN_REVIEW;
import static matgo.global.exception.ErrorCode.NOT_FOUND_MEMBER;
import static matgo.global.exception.ErrorCode.NOT_FOUND_RESTAURANT;
import static matgo.global.exception.ErrorCode.NOT_FOUND_REVIEW;

import lombok.RequiredArgsConstructor;
import matgo.global.type.S3Directory;
import matgo.global.util.S3Util;
import matgo.member.domain.entity.Member;
import matgo.member.domain.repository.MemberRepository;
import matgo.member.exception.MemberException;
import matgo.restaurant.domain.entity.Restaurant;
import matgo.restaurant.domain.repository.RestaurantRepository;
import matgo.restaurant.exception.RestaurantException;
import matgo.review.domain.entity.Review;
import matgo.review.domain.repository.ReviewQueryRepository;
import matgo.review.domain.repository.ReviewRepository;
import matgo.review.dto.request.ReviewCreateRequest;
import matgo.review.dto.response.ReviewCreateResponse;
import matgo.review.dto.response.ReviewResponse;
import matgo.review.exception.ReviewException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReviewService {


    private final ReviewRepository reviewRepository;
    private final ReviewQueryRepository reviewQueryRepository;
    private final RestaurantRepository restaurantRepository;
    private final MemberRepository memberRepository;
    private final S3Util s3Util;

    @Transactional
    public ReviewCreateResponse createReview(Long memberId, Long restaurantId,
      ReviewCreateRequest reviewCreateRequest, MultipartFile reviewImage) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                                                    .orElseThrow(() -> new RestaurantException(NOT_FOUND_RESTAURANT));
        checkCanWriteReview(memberId, restaurant);

        Member member = memberRepository.findById(memberId)
                                        .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
        String imageUrl = s3Util.uploadAndGetImageURL(reviewImage, S3Directory.REVIEW);
        Review review = Review.from(member, restaurant, reviewCreateRequest, imageUrl);
        reviewRepository.save(review);

        return new ReviewCreateResponse(review.getId());
    }

    private void checkCanWriteReview(Long memberId, Restaurant restaurant) {
        if (reviewRepository.existsByMemberIdAndRestaurantId(memberId, restaurant.getId())) {
            throw new ReviewException(ALREADY_WRITTEN_REVIEW);
        }
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReviewDetail(Long reviewId) {
        return reviewQueryRepository.findReviewResponseByIdWithMemberAndRestaurant(reviewId)
                                    .orElseThrow(() -> new ReviewException(NOT_FOUND_REVIEW));
    }
}