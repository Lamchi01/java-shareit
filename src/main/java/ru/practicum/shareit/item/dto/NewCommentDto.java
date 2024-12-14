package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class NewCommentDto {
    @NotBlank
    private String text;
}
