package com.busgo.booking.service;

import com.busgo.booking.SeatLockService;
import com.busgo.booking.model.Booking;
import com.busgo.booking.model.BookingPassenger;
import com.busgo.booking.model.Payment;
import com.busgo.booking.model.TripSeat;
import com.busgo.booking.repo.BookingPassengerRepository;
import com.busgo.booking.repo.BookingRepository;
import com.busgo.booking.repo.PaymentRepository;
import com.busgo.booking.repo.TripSeatRepository;
import com.busgo.trip.TripRepository;
import com.busgo.trip.Trip;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final SeatLockService seatLockService;
    private final TripSeatRepository tripSeatRepo;
    private final BookingRepository bookingRepo;
    private final BookingPassengerRepository passengerRepo;
    private final PaymentRepository paymentRepo;
    private final TripRepository tripRepo;

    public BookingService(SeatLockService seatLockService,
                          TripSeatRepository tripSeatRepo,
                          BookingRepository bookingRepo,
                          BookingPassengerRepository passengerRepo,
                          PaymentRepository paymentRepo,
                          TripRepository tripRepo) {
        this.seatLockService = seatLockService;
        this.tripSeatRepo = tripSeatRepo;
        this.bookingRepo = bookingRepo;
        this.passengerRepo = passengerRepo;
        this.paymentRepo = paymentRepo;
        this.tripRepo = tripRepo;
    }

    @Transactional
    public Booking confirmHoldAndCreateBooking(String holdToken, String paymentReference, UUID userId) {
        // read seats associated with hold before confirming
        List<String> seatKeys = seatLockService.getSeatsForHold(holdToken);
        if (seatKeys == null || seatKeys.isEmpty()) throw new RuntimeException("Hold not found or expired");

        // parse tripId and seat codes from keys (format: seatlock:<tripId>:<seatCode>)
        String exampleKey = seatKeys.get(0);
        String[] parts = exampleKey.split(":" , 3);
        if (parts.length < 3) throw new RuntimeException("Invalid hold mapping");
        String tripIdStr = parts[1];
        UUID tripId = UUID.fromString(tripIdStr);

        // confirm hold atomically via redis script
        boolean confirmed = seatLockService.confirmHold(holdToken);
        if (!confirmed) throw new RuntimeException("Hold confirmation failed or mismatch");

        // ensure trip exists
        Trip trip = tripRepo.findById(tripId).orElseThrow(() -> new RuntimeException("Trip not found"));

        // build booking
        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setTripId(tripId);
        booking.setUserId(userId);
        booking.setStatus("CONFIRMED");

        List<BookingPassenger> passengers = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (String k : seatKeys) {
            String[] p = k.split(":" , 3);
            String seatCode = p[2];
            TripSeat ts = tripSeatRepo.findByTripIdAndSeatCode(tripId, seatCode).orElseThrow(() -> new RuntimeException("Trip seat not found: " + seatCode));
            if (Boolean.TRUE.equals(ts.getIsBooked())) throw new RuntimeException("Seat already booked: " + seatCode);
            ts.setIsBooked(true);
            tripSeatRepo.save(ts);

            BigDecimal fare = ts.getPrice() != null ? ts.getPrice() : BigDecimal.valueOf(trip.getBasePrice() != null ? trip.getBasePrice() : 0.0);
            BookingPassenger bp = new BookingPassenger();
            bp.setId(UUID.randomUUID());
            bp.setBooking(booking);
            bp.setFullName("Passenger"); // placeholder; could be extended to include passenger details
            bp.setSeatCode(seatCode);
            bp.setFare(fare);
            passengers.add(bp);
            total = total.add(fare);
        }

        booking.setTotalAmount(total);
        booking.setCreatedAt(OffsetDateTime.now());
        booking.setUpdatedAt(OffsetDateTime.now());

        bookingRepo.save(booking);
        for (BookingPassenger bp : passengers) {
            bp.setBooking(booking);
            passengerRepo.save(bp);
        }

        // create payment record (simulated)
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setBooking(booking);
        payment.setProvider("SIMULATED");
        payment.setProviderPaymentId(paymentReference != null ? paymentReference : "simulated-");
        payment.setAmount(total);
        payment.setStatus("COMPLETED");
        payment.setCreatedAt(OffsetDateTime.now());
        paymentRepo.save(payment);

        return booking;
    }
}
