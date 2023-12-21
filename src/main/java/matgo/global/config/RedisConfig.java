package matgo.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    // 나중에 캐시 사용하면 주석 풀기
//    @Bean
//    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
//        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
//                                                                       .serializeKeysWith(
//                                                                         SerializationPair.fromSerializer(
//                                                                           new StringRedisSerializer()))
//                                                                       .serializeValuesWith(
//                                                                         SerializationPair.fromSerializer(
//                                                                           new GenericJackson2JsonRedisSerializer()));
//
//        return RedisCacheManager.RedisCacheManagerBuilder
//          .fromConnectionFactory(redisConnectionFactory)
//          .cacheDefaults(configuration)
//          .build();
//    }
}
