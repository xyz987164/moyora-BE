package com.project.moyora.global.security;


import com.project.moyora.app.domain.User;
import com.project.moyora.app.repository.UserRepository;
import com.project.moyora.global.exception.ErrorCode;
import com.project.moyora.global.exception.model.CustomException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.util.Date;
import java.util.Optional;




@Component
@Getter
@Slf4j
public class TokenService {

    private final Key key;
    private final long accessTokenValidityTime;
    private final long refreshTokenValidityTime;
    private final UserRepository userRepository;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    private static final String BEARER = "Bearer ";
    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String EMAIL_CLAIM = "email";

    /**
     * 생성자: JWT 키 및 만료 시간 설정
     */
    public TokenService(UserRepository userRepository,
                        @Value("${jwt.access.expiration}") long accessTokenValidityTime,
                        @Value("${jwt.refresh.expiration}") long refreshTokenValidityTime,
                        @Value("${jwt.secret}") String secretKey) {
        //log.info("✅ jwt.access.expiration: {}", accessTokenValidityTime);
        //log.info("✅ jwt.refresh.expiration: {}", refreshTokenValidityTime);
        //log.info("✅ jwt.secret: {}", secretKey);
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityTime = accessTokenValidityTime;
        this.refreshTokenValidityTime = refreshTokenValidityTime;
        this.userRepository = userRepository;
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(User user) {
        long nowTime = System.currentTimeMillis();
        Date accessTokenExpiredTime = new Date(nowTime + accessTokenValidityTime);

        return Jwts.builder()
                .subject(ACCESS_TOKEN_SUBJECT)
                .claim("userId", user.getId())
                .claim(EMAIL_CLAIM, user.getEmail())
                .claim("role", user.getRoleType().name())
                .issuedAt(new Date())
                .expiration(accessTokenExpiredTime)
                .signWith(key)
                .compact();
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken() {
        long nowTime = System.currentTimeMillis();
        Date refreshTokenExpiredTime = new Date(nowTime + refreshTokenValidityTime);

        return Jwts.builder()
                .subject(REFRESH_TOKEN_SUBJECT)
                .issuedAt(new Date())
                .expiration(refreshTokenExpiredTime)
                .signWith(key)
                .compact();
    }

    /**
     * 토큰 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN_EXCEPTION, "토큰이 만료되었습니다.");
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN_EXCEPTION, "유효하지 않은 토큰입니다.");
        }
    }

    /**
     * 토큰에서 사용자 이메일 추출
     */
    public Optional<String> extractEmail(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Optional.ofNullable(claims.get(EMAIL_CLAIM, String.class));
        } catch (JwtException e) {
            log.error("엑세스 토큰이 유효하지 않습니다.");
            throw new CustomException(ErrorCode.INVALID_TOKEN_EXCEPTION,
                    ErrorCode.INVALID_TOKEN_EXCEPTION.getMessage());
        }
    }

    /**
     * Refresh Token DB 저장
     */
    @Transactional
    public void updateRefreshToken(String email, String refreshToken) {
        userRepository.findByEmail(email)
                .ifPresentOrElse(
                        user -> user.updateRefreshToken(refreshToken),
                        () -> { throw new CustomException(ErrorCode.NOT_FOUND_USER_EXCEPTION,
                                ErrorCode.NOT_FOUND_USER_EXCEPTION.getMessage()); }
                );
    }

    /**
     * HTTP 요청에서 AccessToken 추출
     */
    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(accessToken -> accessToken.startsWith(BEARER))
                .map(accessToken -> accessToken.replace(BEARER, ""));
    }

    /**
     * HTTP 요청에서 RefreshToken 추출
     */
    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    /**
     * AccessToken을 HTTP 응답 헤더에 추가
     */
    @Transactional
    public void sendAccessToken(HttpServletResponse response, String accessToken) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader(accessHeader, accessToken);
        log.info("재발급된 AccessToken : {}", accessToken);
    }

    /**
     * AccessToken & RefreshToken을 HTTP 응답 헤더에 추가
     */
    @Transactional
    public void sendAccessAndRefreshToken(HttpServletResponse response,
                                          String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader(accessHeader, accessToken);
        response.setHeader(refreshHeader, refreshToken);
        log.info("AccessToken, RefreshToken 헤더 설정 완료");
    }

    @Transactional(readOnly = true)
    public User getUserFromToken(String token) {
        // "Bearer " 접두어 제거
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String email = extractEmail(token)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN_EXCEPTION, "토큰에서 이메일 추출 실패"));

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER_EXCEPTION, "사용자 정보 없음"));
    }

}
