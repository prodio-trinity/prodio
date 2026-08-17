package com.prodio.user.application;

import java.util.Objects;

public record SignupCommand(String email, String password, String name) {
    public SignupCommand {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일이 필요합니다.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호가 필요합니다.");
        }
        name = Objects.requireNonNullElse(name, "");
    }
}
