package com.app.filmorate.storage.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserSqlQueries {
    FIND_ALL("""
            SELECT * FROM "user";
            """),
    FIND_BY_ID("""
            SELECT * FROM "user" WHERE id = :id;
            """),
    CREATE("""
            INSERT INTO "user" (EMAIL, LOGIN, NAME, BIRTHDAY)
            VALUES (:email, :login, :name, :birthday);
            """),
    UPDATE("""
            UPDATE "user"
            SET email = :email, login = :login, name = :name, birthday = :birthday
            WHERE "user".id = :id;
            """),
    DELETE("""
            DELETE FROM "user" WHERE id = :id
            """),
    GET_FRIENDS("""
            SELECT u.id, u.email, u.login, u.name, u.birthday
            FROM friends f
            JOIN "user" u ON f.request_to_id = u.id
            WHERE f.request_from_id = :userId
            """),
    GET_COMMON_FRIENDS("""
            SELECT u.* FROM "user" u
            JOIN friends a
              ON a.request_to_id = u.id
            JOIN friends b
              ON a.request_to_id = b.request_to_id
            WHERE a.request_from_id = :userIdA
              AND b.request_from_id = :userIdB;
            """),
    ADD_FRIEND("""
            INSERT INTO FRIENDS (REQUEST_FROM_ID, REQUEST_TO_ID)
            values(:userIdA, :userIdB);
            """),
    REMOVE_FRIEND("""
            DELETE FROM friends
            WHERE request_from_id = :userIdA
            AND request_to_id = :userIdB;
            """);

    private final String query;
}