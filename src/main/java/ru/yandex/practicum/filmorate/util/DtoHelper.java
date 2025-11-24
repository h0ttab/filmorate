package ru.yandex.practicum.filmorate.util;

import java.lang.reflect.Field;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DtoHelper {

    public Object transferFields(Object from, Object to) {
        for (Field field : from.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(to);
                if (value == null) {
                    field.set(to, field.get(from));
                }
            } catch (IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
        }

        return to;
    }
}
