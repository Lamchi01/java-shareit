package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class CommentDto {
    private Long id;
    @NotNull
    private String text;
    private Long itemId;
    private String authorName;
    private LocalDateTime created;
}