package com.project.moyora.global.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtResponse {
    private final String accessToken;
    private final String refreshToken;
}

