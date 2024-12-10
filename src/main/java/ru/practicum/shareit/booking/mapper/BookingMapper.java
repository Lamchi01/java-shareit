package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(booking.getId());
        bookingDto.setItem(ItemMapper.toItemDto(booking.getItem()));
        bookingDto.setBooker(UserMapper.toUserDto(booking.getBooker()));
        bookingDto.setStart(booking.getStart());
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setStatus(booking.getStatus());
        return bookingDto;
    }

    public static Booking toBookingNew(CreateBookingDto createBookingDto, Item item, User booker) {
        Booking booking = new Booking();
        booking.setStart(createBookingDto.getStart());
        booking.setEnd(createBookingDto.getEnd());
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(Status.WAITING);
        return booking;
    }
}