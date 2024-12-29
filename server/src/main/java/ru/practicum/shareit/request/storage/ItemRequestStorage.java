package ru.practicum.shareit.request.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@Repository
public interface ItemRequestStorage extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findAllByRequestorId(long userId);
}