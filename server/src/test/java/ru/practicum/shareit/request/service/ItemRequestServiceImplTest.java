package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.interfaces.ItemRequestService;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class ItemRequestServiceImplTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestStorage itemRequestStorage;

    @Autowired
    private UserStorage userStorage;

    private ItemRequestDto itemRequestDto;
    private Long userId;
    private Long requestId;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user = userStorage.save(user);
        userId = user.getId();

        itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("Test Request");

        itemRequestDto = itemRequestService.addItemRequest(userId, itemRequestDto);
        requestId = itemRequestDto.getId();
    }

    @Test
    void testCreateItemRequest() {
        ItemRequestDto newItemRequestInputDto = new ItemRequestDto();
        newItemRequestInputDto.setDescription("New Test Request");

        ItemRequestDto newItemRequestOutputDto = itemRequestService.addItemRequest(userId, newItemRequestInputDto);
        assertNotNull(newItemRequestOutputDto);
        assertEquals(newItemRequestInputDto.getDescription(), newItemRequestOutputDto.getDescription());
    }

    @Test
    void testGetAll() {
        List<ItemRequestDto> requests = itemRequestService.getAllRequests(userId);
        assertFalse(requests.isEmpty());
        assertEquals(1, requests.size());
        assertEquals(itemRequestDto.getId(), requests.get(0).getId());
    }

    @Test
    void testGetAllDetailedByUser() {
        List<ItemRequestDto> requests = itemRequestService.getAllUserRequests(userId);
        assertFalse(requests.isEmpty());
        assertEquals(1, requests.size());
        assertEquals(itemRequestDto.getId(), requests.get(0).getId());
    }

    @Test
    void testGetOneDetailedById() {
        ItemRequestDto request = itemRequestService.getRequestById(userId, requestId);
        assertNotNull(request);
        assertEquals(itemRequestDto.getId(), request.getId());
        assertEquals(itemRequestDto.getDescription(), request.getDescription());
    }

    @Test
    void testGetOneDetailedByIdNotFound() {
        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(userId, 999L));
    }
}