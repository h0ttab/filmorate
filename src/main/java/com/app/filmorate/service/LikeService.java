package com.app.filmorate.service;

import java.util.*;

import com.app.filmorate.storage.like.LikeDbStorage;
import com.app.filmorate.storage.like.LikeDbStorage.LikeDto;
import com.app.filmorate.util.Validators;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.app.filmorate.model.FeedEventType.LIKE;
import static com.app.filmorate.model.OperationType.ADD;
import static com.app.filmorate.model.OperationType.REMOVE;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeDbStorage likeStorage;
    private final Validators validators;
    private final FeedService feedService;

    public void addLike(Integer filmId, Integer userId) {
        try {
            validators.validateLikeNotExists(filmId, userId, getClass());
            likeStorage.addLike(filmId, userId);
        } catch (ValidationException e) {
            log.warn(e.getMessage());
        }
        feedService.save(userId, LIKE, ADD, filmId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        validators.validateLikeExists(filmId, userId, getClass());
        likeStorage.removeLike(filmId, userId);
        feedService.save(userId, LIKE, REMOVE, filmId);
    }

    public List<Integer> getLikesByFilmId(Integer filmId) {
        return likeStorage.getLikesByFilmId(filmId);
    }

    public Map<Integer, List<Integer>> getLikesByFilmIdList(List<Integer> filmIdList) {
        List<LikeDto> likeDtoList = likeStorage.getLikesByFilmIdList(filmIdList);
        Map<Integer, List<Integer>> filmLikeMap = new HashMap<>();
        likeDtoList.forEach(likeDto -> {
            filmLikeMap.computeIfAbsent(likeDto.filmId(), v -> new ArrayList<>()).add(likeDto.userId());
        });
        return filmLikeMap;
    }
}