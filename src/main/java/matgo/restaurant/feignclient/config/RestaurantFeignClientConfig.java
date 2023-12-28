package matgo.restaurant.feignclient.config;

import static matgo.global.exception.ErrorCode.CANT_PARSE_RESPONSE;

import feign.FeignException;
import feign.Response;
import feign.codec.Decoder;
import java.io.IOException;
import java.lang.reflect.Type;
import lombok.extern.slf4j.Slf4j;
import matgo.restaurant.feignclient.exception.FeignClientException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RestaurantFeignClientConfig {

    @Bean
    public Decoder decoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        return new RestaurantServiceDecoder(new SpringDecoder(messageConverters));
    }

    public static class RestaurantServiceDecoder implements Decoder {

        private final Decoder decoder;

        public RestaurantServiceDecoder(Decoder decoder) {
            this.decoder = decoder;
        }

        @Override
        public Object decode(Response response, Type type) throws IOException, FeignException {

            try {
                return decoder.decode(response, type);
            } catch (Exception e) {
                log.error("Failed to decode response", e);
                throw new FeignClientException(CANT_PARSE_RESPONSE);
            }
        }
    }
}
