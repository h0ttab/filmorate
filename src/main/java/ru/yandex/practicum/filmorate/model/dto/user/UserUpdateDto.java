package ru.yandex.practicum.filmorate.model.dto.user;

import java.time.LocalDate;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateDto {
    @NotNull
    @Positive
    private Integer id;

    @Email
    private String email;

    private String login;

    private String name;

    @PastOrPresent
    private LocalDate birthday;
}