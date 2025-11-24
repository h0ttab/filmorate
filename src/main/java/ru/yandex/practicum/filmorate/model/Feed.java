package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Feed {
    private Long timestamp;
    private Integer userId;
    private FeedEventType eventType;
    private OperationType operation;
    private Integer eventId;
    private Integer entityId;
}