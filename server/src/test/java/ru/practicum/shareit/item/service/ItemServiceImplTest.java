package ru.practicum.shareit.item.service;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.interfaces.ItemService;
import ru.practicum.shareit.item.storage.CommentStorage;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemStorage itemStorage;

    @Autowired
    private CommentStorage commentStorage;

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private ItemRequestStorage itemRequestStorage;

    @Autowired
    private BookingStorage bookingStorage;

    private ItemDto itemDto;
    private CommentDto commentDto;
    private Long userId;
    private Long itemId;
    private Long itemRequestId;
    private Long bookingId;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user = userStorage.save(user);
        userId = user.getId();

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("Test Request");
        itemRequest.setRequestor(user);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest = itemRequestStorage.save(itemRequest);
        itemRequestId = itemRequest.getId();

        itemDto = ItemDto.builder()
                .name("Test Name")
                .description("Test Description")
                .available(true)
                .requestId(itemRequestId)
                .build();
        itemDto = itemService.addItem(itemDto, userId);
        itemId = itemDto.getId();

        commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Test Comment");
        commentDto.setCreated(LocalDateTime.now());

        // Создание завершенной аренды
        Booking booking = new Booking();
        booking.setItem(itemStorage.findById(itemId).orElseThrow());
        booking.setBooker(user);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(Status.APPROVED);
        booking = bookingStorage.save(booking);
        bookingId = booking.getId();
    }

    @Test
    void testGetAllByUserId() {
        List<ItemDto> items = itemService.getAllItems(userId);
        assertFalse(items.isEmpty());
        assertEquals(1, items.size());
        assertEquals(itemDto.getId(), items.get(0).getId());
    }

    @Test
    void testGetById() {
        ItemDto item = itemService.getItem(itemId);
        assertNotNull(item);
        assertEquals(itemDto.getId(), item.getId());
        assertEquals(itemDto.getName(), item.getName());
        assertEquals(itemDto.getDescription(), item.getDescription());
        assertEquals(itemDto.getAvailable(), item.getAvailable());
    }

    @Test
    void testFindByText() {
        List<ItemDto> items = itemService.searchItems("Test", userId);
        assertFalse(items.isEmpty());
        assertEquals(1, items.size());
        assertEquals(itemDto.getId(), items.getFirst().getId());
    }

    @Test
    void testCreateItem() {
        ItemDto newItemInputDto = ItemDto.builder()
                .name("New Test Item")
                .description("New Test Description")
                .available(true)
                .build();

        ItemDto newItemOutputDto = itemService.addItem(newItemInputDto, userId);
        assertNotNull(newItemOutputDto);
        assertEquals(newItemInputDto.getName(), newItemOutputDto.getName());
        assertEquals(newItemInputDto.getDescription(), newItemOutputDto.getDescription());
        assertEquals(newItemInputDto.getAvailable(), newItemOutputDto.getAvailable());
    }

    @Test
    void testUpdateItem() {
        ItemDto updatedItemDto = ItemDto.builder()
                .name("Update Test Item")
                .description("Updated Test Description")
                .available(false)
                .build();

        ItemDto updatedItemOutputDto = itemService.patchItem(updatedItemDto, userId, itemId);
        assertNotNull(updatedItemOutputDto);
        assertEquals(updatedItemDto.getName(), updatedItemOutputDto.getName());
        assertEquals(updatedItemDto.getDescription(), updatedItemOutputDto.getDescription());
        assertEquals(updatedItemDto.getAvailable(), updatedItemOutputDto.getAvailable());
    }

    @Test
    void testUpdateItemWithoutName() {
        ItemDto updatedItemDto = ItemDto.builder()
                .description("Updated Test Description")
                .available(false)
                .build();

        ItemDto updatedItemOutputDto = itemService.patchItem(updatedItemDto, userId, itemId);
        assertNotNull(updatedItemOutputDto);
        assertEquals(itemDto.getName(), updatedItemOutputDto.getName());
        assertEquals(updatedItemDto.getDescription(), updatedItemOutputDto.getDescription());
        assertEquals(updatedItemDto.getAvailable(), updatedItemOutputDto.getAvailable());
    }

    @Test
    void testUpdateItemWithoutDescription() {
        ItemDto updatedItemDto = ItemDto.builder()
                .name("Updated Test Item")
                .available(false)
                .build();

        ItemDto updatedItemOutputDto = itemService.patchItem(updatedItemDto, userId, itemId);
        assertNotNull(updatedItemOutputDto);
        assertEquals(updatedItemDto.getName(), updatedItemOutputDto.getName());
        assertEquals(itemDto.getDescription(), updatedItemOutputDto.getDescription());
        assertEquals(updatedItemDto.getAvailable(), updatedItemOutputDto.getAvailable());
    }

    @Test
    void testCreateComment() {
        CommentDto comment = itemService.addComment(userId, itemId, commentDto.getText());
        assertNotNull(comment);
        assertEquals(commentDto.getText(), comment.getText());
    }

    @Test
    void testCreateCommentWithNonExistentUser() {
        assertThrows(NotFoundException.class, () -> itemService.addComment(999L, itemId, commentDto.getText()));
    }

    @Test
    void testCreateCommentWithNonExistentItem() {
        assertThrows(NotFoundException.class, () -> itemService.addComment(userId, 999L, commentDto.getText()));
    }

    @Test
    void testCreateCommentWithNonBookedItem() {
        // Удаление завершенной аренды для проверки исключения
        bookingStorage.deleteById(bookingId);
        assertThrows(ValidationException.class, () -> itemService.addComment(userId, itemId, commentDto.getText()));
    }

    @Test
    void testUpdateItemWithNonExistentUser() {
        assertThrows(NotFoundException.class, () -> itemService.patchItem(itemDto, 999L, itemId));
    }

    @Test
    void testUpdateItemWithNonExistentItem() {
        assertThrows(NotFoundException.class, () -> itemService.patchItem(itemDto, userId, 999L));
    }

    @Test
    void testUpdateItemWithNonOwnerUser() {
        User newUser = new User();
        newUser.setName("New User");
        newUser.setEmail("newuser@example.com");
        newUser = userStorage.save(newUser);
        Long newUserId = newUser.getId();

        assertThrows(ValidationException.class, () -> itemService.patchItem(itemDto, newUserId, itemId));
    }
}