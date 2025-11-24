package ru.yandex.practicum.filmorate.model.dto.review;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewUpdateDto {
    @NotNull
    @Positive
    private Integer reviewId;

    private String content;

    private Boolean isPositive;
}
