package ru.yandex.practicum.filmorate.service;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.yandex.practicum.filmorate.model.FeedEventType.*;
import static ru.yandex.practicum.filmorate.model.OperationType.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FeedServiceTest {

    private final FeedService feedService;
    private final UserDbStorage userStorage;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setup() {
        // создаём пользователей и запоминаем их реальные id
        user1 = userStorage.create(
                User.builder()
                        .email("u1@mail.com")
                        .login("u1")
                        .name("User1")
                        .birthday(LocalDate.of(1990, 1, 1))
                        .build()
        );
        user2 = userStorage.create(
                User.builder()
                        .email("u2@mail.com")
                        .login("u2")
                        .name("User2")
                        .birthday(LocalDate.of(1991, 2, 2))
                        .build()
        );
        user3 = userStorage.create(
                User.builder()
                        .email("u3@mail.com")
                        .login("u3")
                        .name("User3")
                        .birthday(LocalDate.of(1992, 3, 3))
                        .build()
        );
    }

    @Test
    public void testFindById() {
        // подготавливаем данные только через публичный сервис
        feedService.save(user1.getId(), LIKE, REMOVE, 333);
        feedService.save(user1.getId(), REVIEW, UPDATE, 1);
        feedService.save(user1.getId(), FRIEND, ADD, 2);

        List<Feed> feeds = feedService.findById(user1.getId());

        assertThat(feeds).hasSize(3);

        // проверяем, что есть все три ожидаемые события
        assertThat(feeds)
                .extracting(Feed::getEventType)
                .containsExactlyInAnyOrder(LIKE, REVIEW, FRIEND);

        assertThat(feeds).anySatisfy(feed -> {
            assertThat(feed.getEventType()).isEqualTo(LIKE);
            assertThat(feed.getOperation()).isEqualTo(REMOVE);
            assertThat(feed.getEntityId()).isEqualTo(333);
            assertThat(feed.getUserId()).isEqualTo(user1.getId());
        });

        assertThat(feeds).anySatisfy(feed -> {
            assertThat(feed.getEventType()).isEqualTo(REVIEW);
            assertThat(feed.getOperation()).isEqualTo(UPDATE);
            assertThat(feed.getEntityId()).isEqualTo(1);
            assertThat(feed.getUserId()).isEqualTo(user1.getId());
        });

        assertThat(feeds).anySatisfy(feed -> {
            assertThat(feed.getEventType()).isEqualTo(FRIEND);
            assertThat(feed.getOperation()).isEqualTo(ADD);
            assertThat(feed.getEntityId()).isEqualTo(2);
            assertThat(feed.getUserId()).isEqualTo(user1.getId());
        });
    }

    @Test
    public void testFindAll() {
        // создаём 4 события для двух пользователей
        feedService.save(user1.getId(), LIKE, REMOVE, 333);
        feedService.save(user1.getId(), REVIEW, UPDATE, 1);
        feedService.save(user1.getId(), FRIEND, ADD, 2);
        feedService.save(user2.getId(), REVIEW, ADD, 3);

        List<Feed> feeds = feedService.findAll();

        assertThat(feeds).hasSize(4);

        // проверяем, что есть конкретное событие для user2
        assertThat(feeds).anySatisfy(feed -> {
            assertThat(feed.getUserId()).isEqualTo(user2.getId());
            assertThat(feed.getEventType()).isEqualTo(REVIEW);
            assertThat(feed.getOperation()).isEqualTo(ADD);
            assertThat(feed.getEntityId()).isEqualTo(3);
        });
    }

    @Test
    public void testSaveFeed() {
        // исходно 4 события
        feedService.save(user1.getId(), LIKE, REMOVE, 333);
        feedService.save(user1.getId(), REVIEW, UPDATE, 1);
        feedService.save(user1.getId(), FRIEND, ADD, 2);
        feedService.save(user2.getId(), REVIEW, ADD, 3);

        List<Feed> feedsBefore = feedService.findAll();
        assertThat(feedsBefore).hasSize(4);

        // сохраняем новое событие для user3
        feedService.save(user3.getId(), FRIEND, ADD, 2);

        List<Feed> feedsAfter = feedService.findAll();
        assertThat(feedsAfter).hasSize(5);

        // проверяем, что новое событие действительно появилось
        assertThat(feedsAfter).anySatisfy(feed -> {
            assertThat(feed.getUserId()).isEqualTo(user3.getId());
            assertThat(feed.getEventType()).isEqualTo(FRIEND);
            assertThat(feed.getOperation()).isEqualTo(ADD);
            assertThat(feed.getEntityId()).isEqualTo(2);
        });
    }
}
