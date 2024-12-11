package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.service.interfaces.BookingService;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @PostMapping
    public BookingDto addBooking(@Valid @RequestBody CreateBookingDto createBookingDto,
                           @RequestHeader(HEADER_USER_ID) long userId) {
        if (createBookingDto.getStart().equals(createBookingDto.getEnd())) {
            throw new ValidationException("Дата начала бронирования не может быть равна дате окончания");
        }
        return bookingService.addBooking(createBookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto patchBooking(@PathVariable long bookingId,
                             @RequestParam boolean approved,
                             @RequestHeader(HEADER_USER_ID) long userId) {
        return bookingService.patchBooking(bookingId, userId, approved);
    }

    @GetMapping
    public List<BookingDto> getBookingsByBooker(@RequestHeader(HEADER_USER_ID) long bookerId,
                                          @RequestParam(defaultValue = "ALL") State state) {
        return bookingService.findBookingsByBookerIdAndState(bookerId, state);
    }

    @GetMapping("{bookingId}")
    public BookingDto getBookingByBookerOrOwnerItem(@RequestHeader(HEADER_USER_ID) long userId,
                                               @PathVariable long bookingId) {
        return bookingService.findBookingByOwnerAndBooker(userId, bookingId);
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsByOwner(@RequestHeader(HEADER_USER_ID) long ownerId,
                                               @RequestParam(defaultValue = "ALL") State state) {
        return bookingService.findBookingsByOwnerIdAndState(ownerId, state);
    }
}
