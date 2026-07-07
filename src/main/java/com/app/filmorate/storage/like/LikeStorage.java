package com.app.filmorate.storage.like;

import java.util.List;

import com.app.filmorate.storage.like.LikeDbStorage.LikeDto;

public interface LikeStorage {
    void addLike(Integer filmId, Integer userId);

    void removeLike(Integer filmId, Integer userId);

    List<Integer> getLikesByFilmId(Integer filmId);

    List<LikeDto> getLikesByFilmIdList(List<Integer> filmIdList);
}