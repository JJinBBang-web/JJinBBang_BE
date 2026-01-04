package JJinBBang.app.global.config;

import JJinBBang.app.domain.user.entity.PendingUser;
import JJinBBang.app.global.mail.dto.EmailAuthInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@ConditionalOnProperty( // 조건부 빈 등록 (application.yml의 app.storage.mode 속성에 따라 redis 모드일 때만 활성화)
        prefix = "app.repository",
        name = "mode",
        havingValue = "redis"
)
public class RedisConfig {

    @Bean
    public RedisTemplate<String, EmailAuthInfo> emailAuthRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, EmailAuthInfo> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // key: String serializer
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // value: JSON serializer for EmailAuthInfo
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 생성자 인자로 ObjectMapper와 target Class를 함께 넘김
        Jackson2JsonRedisSerializer<EmailAuthInfo> ser =
                new Jackson2JsonRedisSerializer<>(om, EmailAuthInfo.class);

        template.setValueSerializer(ser);
        template.setHashValueSerializer(ser);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, PendingUser> pendingUserRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, PendingUser> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // key: String serializer
        template.setKeySerializer(new StringRedisSerializer());

        // value: JSON serializer for PendingUser
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 생성자 인자로 ObjectMapper와 target Class를 함께 넘김
        Jackson2JsonRedisSerializer<PendingUser> ser =
                new Jackson2JsonRedisSerializer<>(om, PendingUser.class);

        template.setValueSerializer(ser);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // key: String serializer
        template.setKeySerializer(new StringRedisSerializer());

        // value: JSON serializer for Object (PendingUser 등 일반 객체용)
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonRedisSerializer<Object> serializer =
                new Jackson2JsonRedisSerializer<>(om, Object.class);

        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }


    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    @Bean
    public CommandLineRunner redisHealthCheck(RedisConnectionFactory factory) {
        return args -> {
            String pong = factory.getConnection().ping();
            System.out.println("▶ Redis PING 응답: " + pong);
        };
    }
}