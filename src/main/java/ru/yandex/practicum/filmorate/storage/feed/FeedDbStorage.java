package ru.yandex.practicum.filmorate.storage.feed;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.*;

@Primary
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FeedDbStorage implements FeedStorage {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final FeedRowMapper mapper;

    @Override
    public List<Feed> findAll() {
        return namedParameterJdbcTemplate.query(FeedSqlQueries.FIND_ALL.getQuery(), mapper);
    }

    @Override
    public List<Feed> findById(Integer id) {
        MapSqlParameterSource params = new MapSqlParameterSource("userId", id);
        return namedParameterJdbcTemplate.query(FeedSqlQueries.FIND_BY_USER_ID.getQuery(), params, mapper);
    }

    @Override
    public void save(Integer userId, FeedEventType feedEventType, OperationType operationType,
                     Integer entityId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", Instant.now())
                .addValue("userId", userId)
                .addValue("eventType", feedEventType.toString())
                .addValue("operationType", operationType.toString())
                .addValue("entityId", entityId);

        namedParameterJdbcTemplate.update(FeedSqlQueries.SAVE.getQuery(), params);
    }

    @Component
    @RequiredArgsConstructor
    private static class FeedRowMapper implements RowMapper<Feed> {
        @Override
        public Feed mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            return Feed.builder()
                    .timestamp(resultSet.getTimestamp("DATE").getTime())
                    .userId(resultSet.getInt("USER_ID"))
                    .eventType(FeedEventType.valueOf(resultSet.getString("EVENT_TYPE")))
                    .operation(OperationType.valueOf(resultSet.getString("OPERATION_TYPE")))
                    .eventId(resultSet.getInt("ID"))
                    .entityId(resultSet.getInt("ENTITY_ID"))
                    .build();
        }
    }
}