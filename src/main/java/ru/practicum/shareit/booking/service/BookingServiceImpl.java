package ru.practicum.shareit.booking.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.interfaces.BookingService;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingStorage bookingStorage;
    private final UserStorage userStorage;
    private final ItemStorage itemStorage;

    @Transactional
    @Override
    public BookingDto addBooking(CreateBookingDto createBookingDto, long userId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemStorage.findById(createBookingDto.getItemId())
                        .orElseThrow(() -> new NotFoundException("Предмет не найден"));
        if (createBookingDto.getStart().equals(createBookingDto.getEnd())) {
            throw new ValidationException("Дата начала бронирования не может быть равна дате окончания");
        }
        if (!item.getAvailable()) {
            throw new ValidationException("Предмет недоступен");
        }
        Booking booking = BookingMapper.toBookingNew(createBookingDto, item, user);
        return BookingMapper.toBookingDto(bookingStorage.save(booking));
    }

    @Transactional
    @Override
    public BookingDto patchBooking(long bookingId, long userId, boolean approved) {
        Booking booking = bookingStorage.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
        Item item = itemStorage.findById(booking.getItem().getId())
                .orElseThrow(() -> new NotFoundException("Предмет не найден"));
        if (item.getOwner().getId() != userId) {
            throw new ValidationException("Вы не владелец этого предмета");
        }
        if (booking.getStatus() != Status.WAITING) {
            throw new ValidationException("Бронирование уже подтверждено");
        }
        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        return BookingMapper.toBookingDto(bookingStorage.save(booking));
    }

    @Override
    public List<BookingDto> findBookingsByOwnerIdAndState(long userId, State state) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        List<Booking> bookings = switch (state) {
            case ALL -> bookingStorage.findAllByItemOwnerIdOrderByStartDesc(userId);
            case CURRENT -> bookingStorage.findAllByItemOwnerIdAndStatusAndEndIsAfterOrderByStartDesc(
                    userId, Status.APPROVED, LocalDateTime.now());
            case PAST -> bookingStorage.findAllByItemOwnerIdAndStatusAndEndIsBeforeOrderByStartDesc(
                    userId, Status.APPROVED, LocalDateTime.now());
            case FUTURE -> bookingStorage.findAllByItemOwnerIdAndStatusAndStartIsAfterOrderByStartDesc(
                    userId, Status.APPROVED, LocalDateTime.now());
            case WAITING -> bookingStorage.findAllByItemOwnerIdAndStatusOrderByStartDesc(
                    userId, Status.WAITING);
            case REJECTED -> bookingStorage.findAllByItemOwnerIdAndStatusOrderByStartDesc(
                    userId, Status.REJECTED);
        };
        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    @Override
    public List<BookingDto> findBookingsByBookerIdAndState(long userId, State state) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        List<Booking> bookings = switch (state) {
            case ALL -> bookingStorage.findAllByBookerIdOrderByStartDesc(userId);
            case CURRENT -> bookingStorage.findAllByBookerIdAndStatusAndEndIsAfterOrderByStartDesc(
                    userId, Status.APPROVED, LocalDateTime.now());
            case PAST -> bookingStorage.findAllByBookerIdAndStatusAndEndIsBeforeOrderByStartDesc(
                    userId, Status.APPROVED, LocalDateTime.now());
            case FUTURE -> bookingStorage.findAllByBookerIdAndStatusAndStartIsAfterOrderByStartDesc(
                    userId, Status.APPROVED, LocalDateTime.now());
            case WAITING -> bookingStorage.findAllByBookerIdAndStatusOrderByStartDesc(
                    userId, Status.WAITING);
            case REJECTED -> bookingStorage.findAllByBookerIdAndStatusOrderByStartDesc(
                    userId, Status.REJECTED);
        };
        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    @Override
    public BookingDto findBookingByOwnerAndBooker(long userId, long bookingId) {
        Booking booking = bookingStorage.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
        if (booking.getItem().getOwner().getId() != userId && booking.getBooker().getId() != userId) {
            throw new NotFoundException("Данное бронирование не найдено");
        }
        return BookingMapper.toBookingDto(booking);
    }
}
