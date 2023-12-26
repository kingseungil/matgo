package matgo.restaurant.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matgo.global.entity.BaseEntity;
import matgo.restaurant.feignclient.dto.RestaurantData;
import matgo.review.domain.entity.Review;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
  name = "restaurant",
  indexes = {
    @Index(name = "IDX_name_address", columnList = "name,address")
  }
)
public class Restaurant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

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
    private LocalDateTime approvedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "restaurant")
    private List<Review> reviews = new ArrayList<>();

    public Restaurant(String name, String address, String phoneNumber, Double lat, Double lon, String description) {
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.lat = lat;
        this.lon = lon;
        this.description = description;
    }

    public static Restaurant from(RestaurantData data) {
        data = validate(data);
        return new Restaurant(
          data.name(),
          data.address(),
          data.phoneNumber(),
          data.lat(),
          data.lon(),
          data.description()
        );
    }

    private static RestaurantData validate(RestaurantData data) {
        String name = data.name() != null ? data.name() : "이름 없음";
        String address = data.address() != null ? data.address() : "주소 없음";
        String phoneNumber = data.phoneNumber() != null ? data.phoneNumber() : "전화번호 없음";
        Double lat = data.lat() != null ? data.lat() : 0.0;
        Double lon = data.lon() != null ? data.lon() : 0.0;
        String description = data.description() != null ? data.description() : "설명 없음";

        return new RestaurantData(name, address, phoneNumber, lat, lon, description);
    }

    public void update(Restaurant restaurant) {
        this.name = restaurant.name;
        this.address = restaurant.address;
        this.phoneNumber = restaurant.phoneNumber;
        this.lat = restaurant.lat;
        this.lon = restaurant.lon;
        this.description = restaurant.description;
    }
}
