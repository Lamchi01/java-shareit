package ru.practicum.shareit.item.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.interfaces.ItemService;
import ru.practicum.shareit.item.storage.CommentStorage;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final UserStorage userStorage;
    private final ItemStorage itemStorage;
    private final BookingStorage bookingStorage;
    private final CommentStorage commentStorage;

    @Transactional
    @Override
    public ItemDto addItem(ItemDto itemDto, long userId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = ItemMapper.toItem(itemDto, user);
        return ItemMapper.toItemDto(itemStorage.save(item));
    }

    @Transactional
    @Override
    public ItemDto patchItem(ItemDto itemDto, long userId, long itemId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет не найден"));
        if (item.getOwner().getId() != userId) {
            throw new ValidationException("Вы не владелец этой вещи");
        }
        if (itemDto.getName() != null) item.setName(itemDto.getName());
        if (itemDto.getDescription() != null) item.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null) item.setAvailable(itemDto.getAvailable());
        return ItemMapper.toItemDto(itemStorage.save(item));
    }

    @Override
    public ItemDto getItem(long itemId) {
        Item item = itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет не найден"));
        item.setComments(commentStorage.findAllByItemId(itemId));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllItems(long userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        List<Item> items = itemStorage.findAllByOwnerId(userId);
        Map<Long, List<Comment>> comments = commentStorage.findAllByItemIdIn(items.stream()
                .map(Item::getId)
                .toList()).stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));
        for (Item item : items) {
            item.setComments(comments.getOrDefault(item.getId(), List.of()));
        }
        return items.stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> searchItems(String text, long userId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<Item> items = itemStorage.findAllByNameLikeOrDescriptionLike(text);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto addComment(long authorId, long itemId, String text) {
        User author = userStorage.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет не найден"));
        List<Booking> bookings = bookingStorage.findAllByItemIdAndBookerId(itemId, authorId);
        for (Booking booking : bookings) {
            if (booking.getStatus() == Status.APPROVED && booking.getEnd().isBefore(LocalDateTime.now())) {
                Comment comment = CommentMapper.toComment(text.trim(), author, item, LocalDateTime.now());
                Comment createdComment = commentStorage.save(comment);
                return CommentMapper.toCommentDto(createdComment);
            }
        }
        throw new ValidationException("Невозможно оставить комментарий");
    }
}