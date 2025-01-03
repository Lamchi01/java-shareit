package ru.practicum.shareit.item.service.interfaces;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@Service
public interface ItemService {
    ItemDto addItem(ItemDto itemDto, long userId);

    ItemDto patchItem(ItemDto itemDto, long userId, long itemId);

    ItemDto getItem(long itemId);

    List<ItemDto> getAllItems(long userId);

    List<ItemDto> searchItems(String text, long userId);

    CommentDto addComment(long userId, long itemId, String text);
}
