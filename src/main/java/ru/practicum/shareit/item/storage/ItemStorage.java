package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemStorage extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(Long userId);
    @Query("select i from Item i where (lower(i.name) like ?1 or lower(i.description) like ?1) and i.available = true")
    List<Item> findAllByNameLikeOrDescriptionLike(@Param("text") String text);
}