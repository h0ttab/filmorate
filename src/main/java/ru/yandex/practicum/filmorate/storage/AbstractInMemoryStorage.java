package ru.yandex.practicum.filmorate.storage;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractInMemoryStorage<T> {
    protected final Map<Integer, T> mapEntityStorage = new HashMap<>();
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected int getNextId() {
        int currentMaxId = mapEntityStorage.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}