package matgo.review.application;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import matgo.global.type.Reaction;
import matgo.review.domain.entity.Review;
import matgo.review.domain.entity.ReviewReaction;
import matgo.review.domain.repository.ReviewReactionRepository;
import matgo.review.domain.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ReviewServiceLockTest {

    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ReviewReactionRepository reviewReactionRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    void addReviewReaction_concurrency() throws InterruptedException {
        // given
        int numberOfThreads = 5;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        Long reviewId = 1L;
        Reaction reactionType = Reaction.DISLIKE;
        Long[] memberIds = new Long[]{1L, 4L, 5L, 6L, 7L};
        // when
        for (int i = 0; i < numberOfThreads; i++) {
            int idx = i;
            service.execute(() -> {
                try {
                    Long memberId = memberIds[idx];
                    reviewService.addReviewReaction(memberId, reviewId, reactionType);
                } finally {
                    latch.countDown();
                }
            });
        }

        // then
        latch.await();  // wait for all threads to finish
        service.shutdown();

        List<ReviewReaction> reactions = reviewReactionRepository.findAll().stream()
                                                                 .filter(reviewReaction -> reviewReaction.getReview()
                                                                                                         .getId()
                                                                                                         .equals(
                                                                                                           reviewId))
                                                                 .toList();
        Review review = reviewRepository.findById(reviewId).get();

        assertSoftly(softly -> {
            softly.assertThat(reactions.size()).isEqualTo(5);
            softly.assertThat(review.getLikeCount()).isEqualTo(0);
            softly.assertThat(review.getDislikeCount()).isEqualTo(5);
        });

    }
}
