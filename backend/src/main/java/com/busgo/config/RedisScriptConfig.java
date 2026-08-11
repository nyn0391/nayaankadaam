package com.busgo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class RedisScriptConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(org.springframework.core.env.Environment env) {
        String host = env.getProperty("spring.redis.host", "redis");
        int port = Integer.parseInt(env.getProperty("spring.redis.port", "6379"));
        LettuceConnectionFactory f = new LettuceConnectionFactory(host, port);
        f.afterPropertiesSet();
        return f;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory connectionFactory) {
        StringRedisTemplate t = new StringRedisTemplate(connectionFactory);
        t.setKeySerializer(new StringRedisSerializer());
        t.setValueSerializer(new StringRedisSerializer());
        return t;
    }

    @Bean
    public DefaultRedisScript<String> lockSeatsScript() throws IOException {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setScriptText(readResourceAsString("redis/lock_seats.lua"));
        script.setResultType(String.class);
        return script;
    }

    @Bean
    public DefaultRedisScript<String> confirmHoldScript() throws IOException {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setScriptText(readResourceAsString("redis/confirm_hold.lua"));
        script.setResultType(String.class);
        return script;
    }

    private String readResourceAsString(String path) throws IOException {
        ClassPathResource res = new ClassPathResource(path);
        byte[] b = res.getInputStream().readAllBytes();
        return new String(b, StandardCharsets.UTF_8);
    }
}
