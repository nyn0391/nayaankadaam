package com.busgo.booking;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;

public class SeatLockServiceTest {

    static GenericContainer<?> redis;
    static LettuceConnectionFactory connectionFactory;
    static StringRedisTemplate template;
    static DefaultRedisScript<String> lockScript;
    static DefaultRedisScript<String> confirmScript;

    @BeforeAll
    public static void setup() throws IOException {
        redis = new GenericContainer<>(DockerImageName.parse("redis:7.0.11-alpine")).withExposedPorts(6379);
        redis.start();
        String host = redis.getHost();
        Integer port = redis.getFirstMappedPort();
        connectionFactory = new LettuceConnectionFactory(host, port);
        connectionFactory.afterPropertiesSet();
        template = new StringRedisTemplate(connectionFactory);

        String lockTxt = Files.readString(Path.of("src/main/resources/redis/lock_seats.lua"));
        lockScript = new DefaultRedisScript<>();
        lockScript.setScriptText(lockTxt);
        lockScript.setResultType(String.class);

        String confirmTxt = Files.readString(Path.of("src/main/resources/redis/confirm_hold.lua"));
        confirmScript = new DefaultRedisScript<>();
        confirmScript.setScriptText(confirmTxt);
        confirmScript.setResultType(String.class);
    }

    @AfterAll
    public static void teardown() {
        if (redis != null) redis.stop();
        if (connectionFactory != null) connectionFactory.destroy();
    }

    @Test
    public void testConcurrentHoldsOnlyOneSucceeds() throws InterruptedException, ExecutionException {
        SeatLockService svc = new SeatLockService(template, lockScript, confirmScript);
        String trip = "trip-1";
        List<String> seats = List.of("A1", "A2");

        ExecutorService exec = Executors.newFixedThreadPool(5);
        Callable<SeatLockService.HoldResult> task = () -> svc.holdSeats(trip, seats, "user1");

        int attempts = 5;
        List<Future<SeatLockService.HoldResult>> futures = new CopyOnWriteArrayList<>();
        for (int i = 0; i < attempts; i++) futures.add(exec.submit(task));

        int success = 0;
        for (Future<SeatLockService.HoldResult> f : futures) {
            SeatLockService.HoldResult r = f.get();
            if (r.holdToken() != null) success++;
        }
        exec.shutdown();
        exec.awaitTermination(5, TimeUnit.SECONDS);

        Assertions.assertEquals(1, success, "Only one concurrent hold should succeed");
    }

    @Test
    public void testConfirmAndRelease() {
        SeatLockService svc = new SeatLockService(template, lockScript, confirmScript);
        String trip = "trip-2";
        List<String> seats = List.of("B1");
        SeatLockService.HoldResult r = svc.holdSeats(trip, seats, "user2");
        Assertions.assertNotNull(r.holdToken());
        boolean ok = svc.confirmHold(r.holdToken());
        Assertions.assertTrue(ok);
        // subsequent confirm should fail
        boolean ok2 = svc.confirmHold(r.holdToken());
        Assertions.assertFalse(ok2);
    }
}
