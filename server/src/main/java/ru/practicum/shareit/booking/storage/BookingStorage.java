package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingStorage extends JpaRepository<Booking, Long> {
    List<Booking> findAllByItemOwnerIdOrderByStartDesc(long bookerId);

    List<Booking> findAllByItemOwnerIdAndStatusAndEndIsAfterOrderByStartDesc(long bookerId, Status status, LocalDateTime time);

    List<Booking> findAllByItemOwnerIdAndStatusAndEndIsBeforeOrderByStartDesc(long bookerId, Status status, LocalDateTime time);

    List<Booking> findAllByItemOwnerIdAndStatusAndStartIsAfterOrderByStartDesc(long bookerId, Status status, LocalDateTime time);

    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(long bookerId, Status status);

    List<Booking> findAllByBookerIdOrderByStartDesc(long bookerId);

    List<Booking> findAllByBookerIdAndStatusAndEndIsAfterOrderByStartDesc(long bookerId, Status status, LocalDateTime time);

    List<Booking> findAllByBookerIdAndStatusAndEndIsBeforeOrderByStartDesc(long bookerId, Status status, LocalDateTime time);

    List<Booking> findAllByBookerIdAndStatusAndStartIsAfterOrderByStartDesc(long bookerId, Status status, LocalDateTime time);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(long bookerId, Status status);

    List<Booking> findAllByItemIdAndBookerId(long itemId, long bookerId);

    // Поиск бронирований на пересечение дат
    @Query("select b from Booking b " +
            "where ?1 = b.item.id and 'APPROVED' = b.status and ?2 <= b.end AND ?3 >= b.start")
    List<Booking> findAllByIntersectingStartAndEnd(long itemId, LocalDateTime start, LocalDateTime end);
}