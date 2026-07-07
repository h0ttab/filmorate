package com.app.filmorate.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private final Map<Integer, FriendStatus> friends = new HashMap<>();
    private Integer id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
}