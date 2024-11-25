package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.interfaces.UserService;
import ru.practicum.shareit.user.storage.UserStorage;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    @Override
    public UserDto addUser(UserDto userDto) {
        return userStorage.addUser(userDto);
    }

    @Override
    public UserDto patchUser(UserDto userDto, long userId) {
        return userStorage.patchUser(userDto, userId);
    }

    @Override
    public UserDto getUser(long userId) {
        return userStorage.getUser(userId);
    }

    @Override
    public void deleteUser(long userId) {
        userStorage.deleteUser(userId);
    }
}
