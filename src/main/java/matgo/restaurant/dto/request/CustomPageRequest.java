package matgo.restaurant.dto.request;

import jakarta.validation.constraints.Min;
import java.util.Optional;
import org.springframework.data.domain.Sort;

public record CustomPageRequest(

  @Min(0)
  int page,
  @Min(1)
  int size,
  Optional<Sort.Direction> direction,
  Optional<String> sortBy
) {

    public Sort getSort() {
        if (direction.isEmpty() || sortBy.isEmpty()) {
            return Sort.unsorted();
        } else {
            return Sort.by(direction.get(), sortBy.get());
        }
    }
}
