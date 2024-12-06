package ru.practicum.shareit.user.storage;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.EmailValidException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Repository
public class UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();

    public UserDto addUser(UserDto userDto) {
        if (checkEmail(userDto)) {
            throw new EmailValidException("Email уже используется");
        }
        User user = UserMapper.toUser(userDto);
        user.setId(getNextId());
        log.info("Создан объект User - {}", user);
        users.put(user.getId(), user);
        emails.add(user.getEmail());
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
                throw new EmailValidException("Email уже используется");
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
        emails.remove(getUser(userId).getEmail());
        log.debug("Удалена почта пользователя с id - {}", userId);
        users.remove(userId);
        log.debug("Пользователь с id удалён - {}", userId);
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
        return emails.contains(checkUser.getEmail());
    }

    private boolean checkEmailType(String email) {
        return !email.matches("^[\\w-.]+@[\\w-]+(\\.[\\w-]+)*\\.[a-z]{2,}$");
    }
}