package matgo.global.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
public class RedisConfig {

    private static final String REDISSON_HOST_PREFIX = "redis://";
    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public RedissonClient redissonClient() {
        RedissonClient redisson;
        Config config = new Config();
        config.useSingleServer()
              .setAddress(REDISSON_HOST_PREFIX + host + ":" + port);
        redisson = Redisson.create(config);
        return redisson;
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
