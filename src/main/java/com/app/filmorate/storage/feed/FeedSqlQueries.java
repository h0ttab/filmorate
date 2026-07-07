package com.app.filmorate.storage.feed;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeedSqlQueries {
    FIND_ALL("""
            SELECT * FROM feed
            """),
    FIND_BY_USER_ID("""
            SELECT * FROM feed
            WHERE user_id = :userId
            """),
    SAVE("""
            INSERT INTO feed (date, user_id, event_type, operation_type, entity_id)
            VALUES (:date, :userId, :eventType, :operationType, :entityId);
            """);

    private final String query;
}