package com.app.filmorate.model.dto.film;

import java.time.LocalDate;
import java.util.List;

import com.app.filmorate.model.dto.ObjectIdDto;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
public class FilmCreateDto {
    @NotBlank
    private String name;

    @NotNull
    @Length(max = 200)
    private String description;

    @NotNull
    private LocalDate releaseDate;

    @NotNull
    @Positive
    private Integer duration;

    private List<ObjectIdDto> genres;

    @NotNull
    private ObjectIdDto mpa;

    private List<ObjectIdDto> directors;
}