package matgo.restaurant.domain.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Persistable;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "restaurant")
@Mapping(mappingPath = "elastic/restaurant-search-mapping.json")
@Setting(settingPath = "elastic/restaurant-search-setting.json")
@Getter
@AllArgsConstructor
public class RestaurantSearch implements Persistable<Long> {

    @Id
    private Long id;
    @Field(type = FieldType.Text, name = "name")
    private String name;
    @Field(type = FieldType.Text, name = "address")
    private String address;
    @Field(type = FieldType.Text, name = "phoneNumber")
    private String phoneNumber;
    @Field(type = FieldType.Double, name = "lat")
    private String lat;
    @Field(type = FieldType.Double, name = "lon")
    private String lon;
    @Field(type = FieldType.Text, name = "description")
    private String description;

//    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime createdAt;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return id == null;
    }
}
