package ru.practicum.shareit.booking.service.interfaces;

import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;

import java.util.List;

public interface BookingService {
    BookingDto addBooking(CreateBookingDto createBookingDto, long userId);

    BookingDto patchBooking(long bookingId, long userId, boolean approved);

    List<BookingDto> findBookingsByOwnerIdAndState(long userId, State state);

    List<BookingDto> findBookingsByBookerIdAndState(long userId, State state);

    BookingDto findBookingByOwnerAndBooker(long userId, long bookingId);
}