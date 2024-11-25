package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.interfaces.ItemService;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserStorage userStorage;
    private final ItemStorage itemStorage;

    @Override
    public ItemDto addItem(ItemDto itemDto, long userId) {
        userStorage.getUser(userId);
        return itemStorage.addItem(itemDto, userId);
    }

    @Override
    public ItemDto patchItem(ItemDto itemDto, long userId, long itemId) {
        userStorage.getUser(userId);
        return itemStorage.patchItem(itemDto, userId, itemId);
    }

    @Override
    public ItemDto getItem(long itemId) {
        return itemStorage.getItem(itemId);
    }

    @Override
    public List<ItemDto> getAllItems(long userId) {
        userStorage.getUser(userId);
        return itemStorage.getAllItems(userId);
    }

    @Override
    public List<ItemDto> searchItems(String text, long userId) {
        userStorage.getUser(userId);
        if (text.isBlank()) {
            return List.of();
        }
        return itemStorage.searchItems(text);
    }
}