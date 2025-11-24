package ru.yandex.practicum.filmorate.service;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.dto.ObjectIdDto;
import ru.yandex.practicum.filmorate.model.dto.film.FilmCreateDto;
import ru.yandex.practicum.filmorate.model.dto.review.ReviewCreateDto;
import ru.yandex.practicum.filmorate.model.dto.review.ReviewUpdateDto;
import ru.yandex.practicum.filmorate.model.dto.user.UserCreateDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ReviewServiceIntegrationTest {

    private final ReviewService reviewService;
    private final UserService userService;
    private final FilmService filmService;
    private final JdbcTemplate jdbcTemplate;

    private Integer userId1;
    private Integer userId2;
    private Integer userId3;
    private Integer filmId;

    @BeforeEach
    void setup() {
        // создаём пользователей
        userId1 = userService.create(UserCreateDto.builder()
                .email("u1@mail.com")
                .login("u1")
                .name("User1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build()
        ).getId();

        userId2 = userService.create(UserCreateDto.builder()
                .email("u2@mail.com")
                .login("u2")
                .name("User2")
                .birthday(LocalDate.of(1991, 2, 2))
                .build()
        ).getId();

        userId3 = userService.create(UserCreateDto.builder()
                .email("u3@mail.com")
                .login("u3")
                .name("User3")
                .birthday(LocalDate.of(1992, 3, 3))
                .build()
        ).getId();

        // создаём фильм
        ObjectIdDto mpaDto = new ObjectIdDto();
        mpaDto.setId(1);

        ObjectIdDto genreDto = new ObjectIdDto();
        genreDto.setId(1);

        filmId = filmService.create(FilmCreateDto.builder()
                .name("Film 1")
                .description("desc")
                .duration(100)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .mpa(mpaDto)
                .genres(List.of(genreDto))
                .build()
        ).getId();
    }

    private ReviewCreateDto.ReviewCreateDtoBuilder defaultReviewBuilder() {
        return ReviewCreateDto.builder()
                .content("Новый отзыв о фильме")
                .isPositive(true)
                .userId(userId1)
                .filmId(filmId);
    }

    @Test
    void createReviewShouldInitializeUsefulToZero() {
        Review review = reviewService.create(defaultReviewBuilder().build());
        Review stored = reviewService.findById(review.getReviewId());
        assertThat(stored.getUseful()).isZero();
    }

    @Test
    void addLikeShouldIncreaseUseful() {
        Review review = reviewService.create(defaultReviewBuilder().build());
        reviewService.addLike(review.getReviewId(), userId2);
        Review updated = reviewService.findById(review.getReviewId());
        assertThat(updated.getUseful()).isEqualTo(1);
    }

    @Test
    void addDislikeShouldDecreaseUseful() {
        Review review = reviewService.create(defaultReviewBuilder().build());
        reviewService.addDislike(review.getReviewId(), userId3);
        Review updated = reviewService.findById(review.getReviewId());
        assertThat(updated.getUseful()).isEqualTo(-1);
    }

    @Test
    void removeLikeShouldRestoreUseful() {
        Review review = reviewService.create(defaultReviewBuilder().build());
        reviewService.addLike(review.getReviewId(), userId2);
        reviewService.removeLike(review.getReviewId(), userId2);
        Review updated = reviewService.findById(review.getReviewId());
        assertThat(updated.getUseful()).isZero();
    }

    @Test
    void removeDislikeShouldRestoreUseful() {
        Review review = reviewService.create(defaultReviewBuilder().build());
        reviewService.addDislike(review.getReviewId(), userId3);
        reviewService.removeDislike(review.getReviewId(), userId3);
        Review updated = reviewService.findById(review.getReviewId());
        assertThat(updated.getUseful()).isZero();
    }

    @Test
    void findAllShouldBeSortedByUsefulDesc_forGivenFilmOnly() {
        Review positiveReview = reviewService.create(
                defaultReviewBuilder()
                        .content("Понравилось")
                        .isPositive(true)
                        .build()
        );

        Review negativeReview = reviewService.create(
                defaultReviewBuilder()
                        .content("Не понравилось")
                        .isPositive(false)
                        .build()
        );

        reviewService.addLike(positiveReview.getReviewId(), userId2);
        reviewService.addDislike(negativeReview.getReviewId(), userId3);

        List<Review> all = reviewService.findAll(filmId, 10);

        List<Review> reviews = all.stream()
                .filter(r -> List.of(
                        positiveReview.getReviewId(),
                        negativeReview.getReviewId()
                ).contains(r.getReviewId()))
                .toList();

        assertThat(reviews)
                .extracting(Review::getReviewId)
                .containsExactly(
                        positiveReview.getReviewId(),
                        negativeReview.getReviewId()
                );

        assertThat(reviews)
                .extracting(Review::getUseful)
                .containsExactly(1, -1);
    }

    @Test
    void deleteShouldCascadeFeedback() {
        Review review = reviewService.create(defaultReviewBuilder().build());
        reviewService.addLike(review.getReviewId(), userId2);

        Integer countBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM review_feedback WHERE review_id = ?",
                Integer.class,
                review.getReviewId());

        assertThat(countBefore).isEqualTo(1);

        reviewService.delete(review.getReviewId());

        Integer countAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM review_feedback WHERE review_id = ?",
                Integer.class,
                review.getReviewId());

        assertThat(countAfter).isZero();
    }

    @Test
    void invalidReviewIdShouldThrowNotFound() {
        assertThatThrownBy(() -> reviewService.findById(9999))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> reviewService.addLike(9999, userId1))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void invalidUserIdShouldThrowNotFound() {
        Review review = reviewService.create(defaultReviewBuilder().build());
        assertThatThrownBy(() -> reviewService.addLike(review.getReviewId(), 9999))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateShouldModifyProvidedFields() {
        Review review = reviewService.create(defaultReviewBuilder().build());
        ReviewUpdateDto updateDto = ReviewUpdateDto.builder()
                .reviewId(review.getReviewId())
                .content("Обновленный текст")
                .build();

        Review updated = reviewService.update(updateDto);
        assertThat(updated.getContent()).isEqualTo("Обновленный текст");

        Review fromDb = reviewService.findById(review.getReviewId());
        assertThat(fromDb).usingRecursiveComparison().isEqualTo(updated);
    }
}
