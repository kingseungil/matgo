package matgo.restaurant.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matgo.global.entity.BaseEntity;
import matgo.restaurant.dto.request.RestaurantRequest;
import matgo.restaurant.feignclient.dto.RestaurantData;
import matgo.review.domain.entity.Review;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
  name = "restaurant",
  uniqueConstraints = {
    @UniqueConstraint(
      name = "uk_restaurant_external_id",
      columnNames = {"external_id"})
  }
)
public class Restaurant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "road_address", nullable = false)
    private String roadAddress;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "lat", nullable = false)
    private Double lat;

    @Column(name = "lon", nullable = false)
    private Double lon;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rating", nullable = false)
    private Double rating;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount;

    @OneToMany(mappedBy = "restaurant")
    private List<Review> reviews = new ArrayList<>();

    public static Restaurant fromRestaurantData(RestaurantData data) {
        data = validate(data);
        return Restaurant.builder()
                         .externalId(data.externalId())
                         .name(data.name())
                         .roadAddress(data.roadAddress())
                         .address(data.address())
                         .phoneNumber(data.phoneNumber())
                         .lat(data.lat())
                         .lon(data.lon())
                         .description(data.description())
                         .approvedAt(LocalDateTime.now())
                         .rating(0.0)
                         .reviewCount(0)
                         .build();
    }

    private static RestaurantData validate(RestaurantData data) {
        String externalId = data.externalId() != null ? data.externalId() : "아이디 없음";
        String name = data.name() != null ? data.name() : "이름 없음";
        String roadAddress = data.roadAddress() != null ? data.roadAddress() : "도로명 주소 없음";
        String address = data.address() != null ? data.address() : "주소 없음";
        String phoneNumber = data.phoneNumber() != null ? data.phoneNumber() : "전화번호 없음";
        Double lat = data.lat() != null ? data.lat() : 0.0;
        Double lon = data.lon() != null ? data.lon() : 0.0;
        String description = data.description() != null ? data.description() : "설명 없음";

        return new RestaurantData(externalId, name, roadAddress, address, phoneNumber, lat, lon, description);
    }

    public static Restaurant fromRestaurantRequest(RestaurantRequest request) {
        // TODO: externalId 생성 정책
        String externalId = UUID.randomUUID().toString();
        return Restaurant.builder()
                         .externalId(externalId)
                         .name(request.name())
                         .roadAddress(request.roadAddress())
                         .address(request.address())
                         .phoneNumber(request.phoneNumber())
                         .lat(request.lat())
                         .lon(request.lon())
                         .description(request.description())
                         .rating(0.0)
                         .reviewCount(0)
                         .build();
    }

    public void update(Restaurant restaurant) {
        this.name = restaurant.name;
        this.address = restaurant.address;
        this.phoneNumber = restaurant.phoneNumber;
        this.lat = restaurant.lat;
        this.lon = restaurant.lon;
        this.description = restaurant.description;
    }

    public void approve() {
        this.approvedAt = LocalDateTime.now();
    }

    public void addReview(Review review) {
        this.reviews.add(review);
        this.reviewCount++;
        updateRating(review, false);
    }

    public void removeReview(Review review) {
        this.reviews.remove(review);
        this.reviewCount--;
        updateRating(review, true);
    }

    private void updateRating(Review review, boolean isRemoved) {
        if (isRemoved) {
            if (this.reviewCount == 0) {
                this.rating = 0.0;
            } else {
                this.rating = ((this.rating * (this.reviewCount + 1)) - review.getRating()) / this.reviewCount;
            }
        } else {
            this.rating = ((this.rating * (this.reviewCount - 1)) + review.getRating()) / this.reviewCount;
        }

        this.rating = Math.round(this.rating * 100) / 100.0;
    }

}
