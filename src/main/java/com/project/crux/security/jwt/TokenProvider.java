package com.project.crux.security.jwt;

import com.project.crux.exception.CustomException;
import com.project.crux.exception.ErrorCode;
import com.project.crux.member.domain.Member;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class TokenProvider {

    private static final String BEARER_PREFIX = "Bearer ";
    private final Key key;
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24;            // 1일
//  private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30;            // 30분
//  private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 5;            // 5초


    public TokenProvider(@Value("${jwt.secret}") String secretKey) {

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }


    //Token 생성
    public String generateToken(Member member) {

        long now = (new Date().getTime());
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .claim("imgUrl", member.getImgUrl())
                .setId(member.getNickname())
                .setSubject(member.getEmail())
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    // Token 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token, 만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }

    /**
     * Jwt Token을 복호화 하여 이름을 얻는다.
     */
    public String getNickname(String token) {
        Jws<Claims> claimsJws;
        try {
            claimsJws = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return claimsJws.getBody().getId();
    }

    public String getImgUrl(String token) {
        Jws<Claims> claimsJws;
        try {
            claimsJws = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return claimsJws.getBody().get("imgUrl", String.class);
    }

    public String extractToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

}
