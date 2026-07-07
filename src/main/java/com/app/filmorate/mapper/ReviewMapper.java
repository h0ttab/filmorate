package com.app.filmorate.mapper;

import com.app.filmorate.model.Review;
import com.app.filmorate.model.dto.review.ReviewCreateDto;
import com.app.filmorate.model.dto.review.ReviewUpdateDto;
import com.app.filmorate.util.Validators;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class ReviewMapper {

    protected Validators validators;

    @Autowired
    public void setValidators(Validators validators) {
        this.validators = validators;
    }

    @BeforeMapping
    protected void validateCreate(ReviewCreateDto dto) {
        validators.validateUserExists(dto.getUserId(), getClass());
        validators.validateFilmExists(dto.getFilmId(), getClass());
    }

    @BeforeMapping
    protected void validateUpdate(ReviewUpdateDto dto) {
        validators.validateReviewExists(dto.getReviewId(), getClass());
    }

    // Создание отзыва
    @Mapping(target = "reviewId", ignore = true)
    @Mapping(target = "useful", constant = "0")
    public abstract Review toEntity(ReviewCreateDto dto);

    // Обновление отзыва
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "filmId", ignore = true)
    @Mapping(target = "useful", ignore = true)
    public abstract Review toEntity(ReviewUpdateDto dto);
}
