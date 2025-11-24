package ru.yandex.practicum.filmorate.model.dto.film;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DirectorDto {
    @NotBlank
    private String name;
}
