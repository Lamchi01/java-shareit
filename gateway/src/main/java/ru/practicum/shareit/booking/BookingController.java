package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.dto.State;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingClient bookingClient;
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader(HEADER_USER_ID) long userId,
                                             @RequestBody @Valid CreateBookingDto createBookingDto) {
        if (createBookingDto.getStart().equals(createBookingDto.getEnd())) {
            throw new ValidationException("Дата начала бронирования не может быть равна дате окончания");
        }
        return bookingClient.addBooking(userId, createBookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> patchBooking(@RequestHeader(HEADER_USER_ID) long userId,
                                               @PathVariable long bookingId,
                                               @RequestParam boolean approved) {
        return bookingClient.patchBooking(userId, bookingId, approved);
    }

    @GetMapping
    public ResponseEntity<Object> getBookingsByBooker(@RequestHeader(HEADER_USER_ID) long bookerId,
                                                      @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                      @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") int from,
                                                      @Positive @RequestParam(name = "size", defaultValue = "10") int size) {
        State state = State.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.getBookingsByBooker(bookerId, state, from, size);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingByBookerOrOwnerItem(@RequestHeader(HEADER_USER_ID) long userId,
                                                                @PathVariable long bookingId) {
        return bookingClient.getBookingByBookerOrOwnerItem(userId, bookingId);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsByOwner(@RequestHeader(HEADER_USER_ID) long ownerId,
                                                     @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                     @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") int from,
                                                     @Positive @RequestParam(name = "size", defaultValue = "10") int size) {
        State state = State.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.getBookingsByOwner(ownerId, state, from, size);
    }

}
