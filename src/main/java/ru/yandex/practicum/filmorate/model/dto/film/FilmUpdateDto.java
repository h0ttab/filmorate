package ru.yandex.practicum.filmorate.model.dto.film;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import ru.yandex.practicum.filmorate.model.dto.ObjectIdDto;

@Data
@Builder
public class FilmUpdateDto {
    @NotNull
    @Positive
    private Integer id;

    private String name;

    @Length(max = 200)
    private String description;

    private LocalDate releaseDate;

    @Positive
    private Integer duration;

    private List<ObjectIdDto> genres;

    private ObjectIdDto mpa;

    private List<ObjectIdDto> directors;
}