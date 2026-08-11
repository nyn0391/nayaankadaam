package com.busgo.booking;

import com.busgo.booking.model.Booking;
import com.busgo.booking.model.Payment;
import com.busgo.booking.model.TripSeat;
import com.busgo.booking.repo.BookingRepository;
import com.busgo.booking.repo.PaymentRepository;
import com.busgo.booking.repo.TripSeatRepository;
import com.busgo.trip.Trip;
import com.busgo.trip.TripRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@SpringBootTest
public class BookingIntegrationTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb").withUsername("test").withPassword("test");
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.0.11-alpine")).withExposedPorts(6379);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry reg) {
        postgres.start();
        redis.start();
        String jdbc = postgres.getJdbcUrl();
        reg.add("spring.datasource.url", () -> jdbc);
        reg.add("spring.datasource.username", () -> postgres.getUsername());
        reg.add("spring.datasource.password", () -> postgres.getPassword());
        reg.add("spring.redis.host", () -> redis.getHost());
        reg.add("spring.redis.port", () -> redis.getFirstMappedPort());
    }

    @AfterAll
    static void tearDown() {
        try { postgres.stop(); } catch (Exception ignored) {}
        try { redis.stop(); } catch (Exception ignored) {}
    }

    @Autowired
    SeatLockService seatLockService;

    @Autowired
    TripRepository tripRepository;

    @Autowired
    TripSeatRepository tripSeatRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    com.busgo.booking.service.BookingService bookingService;

    @Test
    public void testHoldConfirmCreatesBookingAndMarksSeatsBooked() {
        // create a trip
        Trip trip = new Trip();
        UUID tripId = UUID.randomUUID();
        trip.setId(tripId);
        trip.setBasePrice(100.0);
        tripRepository.save(trip);

        // create trip seats
        TripSeat s1 = new TripSeat();
        s1.setId(UUID.randomUUID());
        s1.setTripId(tripId);
        s1.setSeatCode("A1");
        s1.setIsBooked(false);
        s1.setPrice(BigDecimal.valueOf(120));
        tripSeatRepository.save(s1);

        TripSeat s2 = new TripSeat();
        s2.setId(UUID.randomUUID());
        s2.setTripId(tripId);
        s2.setSeatCode("A2");
        s2.setIsBooked(false);
        s2.setPrice(BigDecimal.valueOf(120));
        tripSeatRepository.save(s2);

        // hold seats
        String userId = UUID.randomUUID().toString();
        var hold = seatLockService.holdSeats(tripId.toString(), List.of("A1", "A2"), userId);
        Assertions.assertNotNull(hold.holdToken(), "hold should succeed");

        // confirm and create booking
        UUID userUuid = UUID.randomUUID();
        var booking = bookingService.confirmHoldAndCreateBooking(hold.holdToken(), "payref-123", userUuid);
        Assertions.assertNotNull(booking);
        Assertions.assertEquals("CONFIRMED", booking.getStatus());

        // bookings persisted
        var maybe = bookingRepository.findById(booking.getId());
        Assertions.assertTrue(maybe.isPresent());

        // payments persisted
        List<Payment> payments = paymentRepository.findAll();
        Assertions.assertFalse(payments.isEmpty());
        Payment p = payments.get(0);
        Assertions.assertEquals(booking.getId(), p.getBooking().getId());
        Assertions.assertEquals(BigDecimal.valueOf(240), p.getAmount());

        // trip seats marked booked
        var ts1 = tripSeatRepository.findByTripIdAndSeatCode(tripId, "A1");
        var ts2 = tripSeatRepository.findByTripIdAndSeatCode(tripId, "A2");
        Assertions.assertTrue(ts1.isPresent() && ts1.get().getIsBooked());
        Assertions.assertTrue(ts2.isPresent() && ts2.get().getIsBooked());
    }
}
