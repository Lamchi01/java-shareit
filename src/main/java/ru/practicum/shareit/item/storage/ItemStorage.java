package ru.practicum.shareit.item.storage;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class ItemStorage {
    private final Map<Long, Item> itemIdKey = new HashMap<>();
    private final Map<Long, List<Item>> itemOwnerIdKey = new HashMap<>();

    public ItemDto addItem(ItemDto itemDto, long userId) {
        Item item = new Item();
        item.setId(getNextId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(userId);
        log.info("Создан объект Item - {}", item);
        itemIdKey.put(item.getId(), item);
        List<Item> items = itemOwnerIdKey.getOrDefault(userId, new ArrayList<>());
        items.add(item);
        itemOwnerIdKey.put(item.getOwner(), items);
        return ItemMapper.toItemDto(item);
    }

    public ItemDto patchItem(ItemDto itemDto, long userId, long itemId) {
        if (!itemOwnerIdKey.containsKey(userId)) {
            throw new NotFoundException("Данного пользователя нет");
        }
        if (!itemIdKey.containsKey(itemId)) {
            throw new NotFoundException("Вещь не найден");
        }

        Item item = itemIdKey.get(itemId);
        if (item.getOwner() != userId) {
            throw new ValidationException("Вы не владелец этой вещи");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
            log.debug("Присвоение нового имени вещи");
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
            log.debug("Присвоение нового описания вещи");
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
            log.debug("Присвоение нового статуса бронирования вещи");
        }
        itemIdKey.put(item.getId(), item);
        List<Item> items = itemOwnerIdKey.get(userId);
        itemOwnerIdKey.put(item.getOwner(), items.stream()
                .filter(item1 -> item1.getId() == itemId)
                .peek(item1 -> {
                    item1.setName(item.getName());
                    item1.setDescription(item.getDescription());
                    item1.setAvailable(item.getAvailable());
                })
                .toList());
        return ItemMapper.toItemDto(item);
    }

    public ItemDto getItem(long itemId) {
        if (!itemIdKey.containsKey(itemId)) {
            throw new ValidationException("Вещь не найдена");
        }
        return ItemMapper.toItemDto(itemIdKey.get(itemId));
    }

    public List<ItemDto> getAllItems(long userId) {
        if (!itemOwnerIdKey.containsKey(userId)) {
            throw new ValidationException("Вы не владелец этих вещей");
        }
        return itemOwnerIdKey.get(userId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    public List<ItemDto> searchItems(String text) {
        return itemIdKey.values().stream()
                .filter(item -> (item.getName().toLowerCase().contains(text.toLowerCase()) ||
                        item.getDescription().toLowerCase().contains(text.toLowerCase())) &&
                        item.getAvailable())
                .map(ItemMapper::toItemDto)
                .toList();
    }

    private long getNextId() {
        long currentMaxId = itemIdKey.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
