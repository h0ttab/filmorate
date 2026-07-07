package com.app.filmorate.mapper;

import com.app.filmorate.model.User;
import com.app.filmorate.model.dto.user.UserCreateDto;
import com.app.filmorate.model.dto.user.UserUpdateDto;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    User toEntity(UserCreateDto dto);

    User toEntity(UserUpdateDto dto);

    @AfterMapping
    default void applyNameFallback(UserCreateDto dto, @MappingTarget User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(dto.getLogin());
        }
    }

    @AfterMapping
    default void applyNameFallback(UserUpdateDto dto, @MappingTarget User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(dto.getLogin());
        }
    }
}
