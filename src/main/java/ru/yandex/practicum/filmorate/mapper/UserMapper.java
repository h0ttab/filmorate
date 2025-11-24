package ru.yandex.practicum.filmorate.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.dto.user.UserCreateDto;
import ru.yandex.practicum.filmorate.model.dto.user.UserUpdateDto;

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
