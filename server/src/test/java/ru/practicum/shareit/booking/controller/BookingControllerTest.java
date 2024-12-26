package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.State;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.interfaces.BookingService;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private UserStorage userStorage;

    @MockBean
    private ItemStorage itemStorage;

    @MockBean
    BookingStorage bookingStorage;

    @Autowired
    private MockMvc mvc;

    private Booking booking;
    private CreateBookingDto createBookingDto;
    private BookingDto bookingDto;
    private Item item;
    private ItemDto itemDto;
    private User user;
    private UserDto userDto;
    private List<BookingDto> bookingDtoList;
    private List<ItemDto> itemDtoList;
    private List<UserDto> userDtoList;

    private static final Long ENTITIES_COUNT = 10L;
    private static final Random RANDOM = new Random();

    @BeforeEach
    void setUp() {

        itemDto = ItemDto.builder()
                .id(1L)
                .name(generateRandomString(5))
                .description(generateRandomString(5))
                .available(true)
                .requestId(null)
                .lastBooking(null)
                .nextBooking(null)
                .comments(null)
                .build();

        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setRequest(null);

        itemDtoList = new ArrayList<>();
        for (long i = 1; i <= ENTITIES_COUNT; i++) {
            itemDtoList.add(ItemDto.builder()
                    .id(i)
                    .name(generateRandomString(5))
                    .description(generateRandomString(5))
                    .available(true)
                    .lastBooking(null)
                    .nextBooking(null)
                    .comments(null)
                    .build());
        }

        userDto = UserDto.builder()
                .id(1L)
                .name(generateRandomString(5))
                .email(generateRandomEmail(5))
                .build();

        User user = new User();
        user.setId(userDto.getId());
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());

        userDtoList = new ArrayList<>();
        for (long i = 1; i <= ENTITIES_COUNT; i++) {
            userDtoList.add(UserDto.builder()
                    .id(i)
                    .name(generateRandomString(5))
                    .email(generateRandomEmail(5))
                    .build());
        }

        bookingDto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusMinutes(1))
                .end(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusDays(1))
                .item(itemDto)
                .booker(userDto)
                .status(Status.WAITING)
                .build();

        booking = new Booking();
        booking.setId(bookingDto.getId());
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(bookingDto.getStatus());

        createBookingDto = new CreateBookingDto();
        createBookingDto.setStart(bookingDto.getStart());
        createBookingDto.setEnd(bookingDto.getEnd());
        createBookingDto.setItemId(bookingDto.getItem().getId());

        bookingDtoList = new ArrayList<>();
        for (long i = 1; i <= ENTITIES_COUNT; i++) {
            bookingDtoList.add(BookingDto.builder()
                    .id(i)
                    .start(LocalDateTime.now().plusMinutes(1))
                    .end(LocalDateTime.now().plusDays(1))
                    .item(itemDto)
                    .booker(userDto)
                    .status(Status.WAITING)
                    .build());
        }
    }

    @Test
    void createBookingTest() throws Exception {
        Long userId = 1L;

        when(bookingService.addBooking(any(), anyLong()))
                .thenReturn(bookingDto);

        mvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(createBookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                .header("X-Sharer-User-Id", bookingDto.getBooker().getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().truncatedTo(ChronoUnit.SECONDS).toString())))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().truncatedTo(ChronoUnit.SECONDS).toString())))
                .andExpect(jsonPath("$.item.id", is(bookingDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(bookingDto.getItem().getName())))
                .andExpect(jsonPath("$.booker.id", is(bookingDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(bookingDto.getBooker().getName())))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().name())));

        verify(bookingService, times(1)).addBooking(eq(createBookingDto), eq(userId));
    }

    @Test
    void updateBookingStatusTest() throws Exception {
        Long bookingId = 1L;
        Long userId = 1L;
        Boolean approved = true;
        bookingDto.setStatus(Status.APPROVED);

        when(bookingService.patchBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(bookingDto);

        mvc.perform(patch("/bookings/" + bookingDto.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", bookingDto.getBooker().getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().name())));

        verify(bookingService, times(1)).patchBooking(eq(bookingId), eq(userId), eq(approved));
    }

    @Test
    void getBookingByIdTest() throws Exception {
        when(bookingService.findBookingByOwnerAndBooker(anyLong(), anyLong()))
                .thenReturn(bookingDto);

        mvc.perform(get("/bookings/" + bookingDto.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", bookingDto.getBooker().getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class));

        verify(bookingService, times(1)).findBookingByOwnerAndBooker(eq(bookingDto.getId()), eq(bookingDto.getBooker().getId()));
    }

    @Test
    void getAllUsersBookingByStatusTest() throws Exception {
        Long userId = 1L;

        when(bookingService.findBookingsByBookerIdAndState(Mockito.anyLong(), Mockito.any()))
                .thenReturn(bookingDtoList);

        mvc.perform(get("/bookings")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", bookingDto.getBooker().getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(bookingDtoList.size())))
                .andExpect(jsonPath("$[0].id", is(bookingDtoList.getFirst().getId().intValue())));

        verify(bookingService, times(1)).findBookingsByBookerIdAndState(eq(userId), eq(State.ALL));
    }

    @Test
    void getAllBookingForUserItemsByStatusTest() throws Exception {
        Long userId = 1L;

        when(bookingService.findBookingsByOwnerIdAndState(anyLong(), any()))
                .thenReturn(bookingDtoList);

        mvc.perform(get("/bookings/owner")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", bookingDto.getBooker().getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()", is(bookingDtoList.size())))
                .andExpect(jsonPath("$[0].id", is(bookingDtoList.getFirst().getId().intValue())));

        verify(bookingService, times(1)).findBookingsByOwnerIdAndState(eq(userId), eq(State.ALL));
    }

    private String generateRandomString(int targetStringLength) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        return RANDOM.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private String generateRandomEmail(int targetStringLength) {
        return generateRandomString(targetStringLength) + "@example.com";
    }

    private LocalDateTime generateRandomDate() {
        long minDay = LocalDateTime.of(2000, 1, 1, 0, 0).toEpochSecond(ZoneOffset.UTC);
        long maxDay = LocalDateTime.of(2023, 12, 31, 23, 59).toEpochSecond(ZoneOffset.UTC);
        long randomDay = minDay + RANDOM.nextLong() % (maxDay - minDay);
        return LocalDateTime.ofEpochSecond(randomDay, 0, ZoneOffset.UTC);
    }
}