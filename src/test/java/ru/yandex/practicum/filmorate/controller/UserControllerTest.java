package ru.yandex.practicum.filmorate.controller;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.filmorate.config.ControllerTest;
import ru.yandex.practicum.filmorate.model.User;

@ControllerTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserControllerTest {
    private final WebTestClient webTestClient;

    @Test
    void shouldFailOnInvalidInput_createUserTest_emptyUser() {
        User user = User.builder().build();
        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(User.builder().build())
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    void shouldFailOnInvalidInput_createUserTest_invalidEmail() {
        User user = User.builder()
                .email("abc.invalid")
                .login("Login")
                .name("Alex")
                .birthday(LocalDate.of(1990, 12, 10))
                .build();
        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    void shouldFailOnInvalidInput_createUserTest_blankNullLogin() {
        User userBlankLogin = User.builder()
                .email("abc.valid@email.com")
                .login("")
                .name("Alex")
                .birthday(LocalDate.of(1990, 12, 10))
                .build();
        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userBlankLogin)
                .exchange()
                .expectStatus()
                .is4xxClientError();

        User userNullLogin = User.builder()
                .email("abc.valid@email.com")
                .login(null)
                .name("Alex")
                .birthday(LocalDate.of(1990, 12, 10))
                .build();
        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userNullLogin)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    void shouldFailOnInvalidInput_createUserTest_invalidBirthDate() {
        User user = User.builder()
                .email("abc.valid@email.com")
                .login("Login")
                .name("Alex")
                .birthday(LocalDate.of(9999, 12, 10))
                .build();
        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    void shouldFailOnInvalidInput_createUserTest_missingFields() {
        User user = User.builder()
                .login("Login")
                .name("Alex")
                .build();
        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    void shouldNotFailOnValidInput_createUserTest_validRequest() {
        User user = User.builder()
                .email("abc.valid@email.com")
                .login("Login")
                .name("Alex")
                .birthday(LocalDate.of(1990, 12, 10))
                .build();
        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }
}


