package com.app.filmorate.storage;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.app.filmorate.model.Mpa;
import com.app.filmorate.storage.mpa.MpaDbStorage;
import com.app.filmorate.testutil.TestDataUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(MpaDbStorage.class)
public class MpaStorageTest {

    private final MpaDbStorage storage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void beforeEach() {
        TestDataUtil.seedAllBase(jdbcTemplate);
    }

    @Test
    void testFindById() {
        Mpa mpa = storage.findById(5);

        assertThat(mpa)
                .hasFieldOrPropertyWithValue("id", 5)
                .hasFieldOrPropertyWithValue("name", "NC-17");
    }

    @Test
    void testFindAll() {
        List<Mpa> mpaSet = storage.findAll();

        assertThat(mpaSet.size()).isEqualTo(5);
        assertThat(mpaSet).allSatisfy(mpa ->
                assertThat(mpa)
                        .hasFieldOrProperty("id")
                        .hasFieldOrProperty("name")
        );
    }
}
