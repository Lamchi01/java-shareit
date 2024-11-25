package ru.practicum.shareit.user.storage;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Repository
public class UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    public UserDto addUser(UserDto userDto) {
        if (checkEmail(userDto)) {
            throw new ValidationException("Email уже используется");
        }
        User user = new User();
        user.setId(getNextId());
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        log.info("Создан объект User - {}", user);
        users.put(user.getId(), user);
        return UserMapper.toUserDto(user);
    }

    public UserDto getUser(long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Такого пользователя нет");
        }
        return UserMapper.toUserDto(users.get(userId));
    }

    public UserDto patchUser(UserDto userDto, long userId) {
        User user = users.get(userId);
        if (userDto.getName() != null) user.setName(userDto.getName());
        log.debug("Присвоение нового имени пользователя");
        if (userDto.getEmail() != null) {
            if (checkEmail(userDto)) {
                throw new ValidationException("Email уже используется");
            }
            if (checkEmailType(userDto.getEmail())) {
                throw new ValidationException("Неподходящий email");
            }
            user.setEmail(userDto.getEmail());
            log.debug("Присвоение нового email пользователя - {}", user);
        }
        users.put(userId, user);
        return UserMapper.toUserDto(user);
    }

    public void deleteUser(long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Такого пользователя нет");
        }
        users.remove(userId);
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private boolean checkEmail(UserDto checkUser) {
        return users.values().stream()
                .map(User::getEmail)
                .anyMatch(checkUser.getEmail()::equals);
    }

    public boolean checkEmailType(String email) {
        return !email.matches("^[\\w-.]+@[\\w-]+(\\.[\\w-]+)*\\.[a-z]{2,}$");
    }
}