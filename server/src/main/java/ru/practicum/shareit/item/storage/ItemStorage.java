package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemStorage extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(Long userId);

    @Query("select i " +
            "from Item i " +
            "where (LOWER(i.name) like LOWER(CONCAT('%', :text, '%')) " +
            "or LOWER(i.description) like LOWER(CONCAT('%', :text, '%'))) " +
            "and i.available = true")
    List<Item> findAllByNameLikeOrDescriptionLike(String text);

    List<Item> findAllByRequestId(Long requestId);
}