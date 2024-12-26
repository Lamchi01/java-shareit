package ru.practicum.shareit.exceptions;

public class EmailValidException extends RuntimeException {
    public EmailValidException(String message) {
        super(message);
    }
}
