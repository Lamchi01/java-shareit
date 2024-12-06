package ru.practicum.shareit.user.service.interfaces;

import ru.practicum.shareit.user.dto.UserDto;

public interface UserService {
    UserDto addUser(UserDto userDto);

    UserDto patchUser(UserDto userDto, long userId);

    UserDto getUser(long userId);

    void deleteUser(long userId);
}