package com.example.zzyzzy.semiprojectv1.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secretKey}")
    private String secretString;
    @Value("${jwt.validity}")
    private Long validity;

    private Key secretKey;

    // jwt 생성시 특수문자가 포함된 비밀키를 사용하게 해 줌
    @PostConstruct          // 의존성 주입 후 초기화
    protected void init() {
        // secretString 의 내용을 byte 배열로 변환 후
        byte[] keyBytes = secretString.getBytes();
        // JWT의 안전한 key 생성 방식으로 key 생성
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }


    // 주어진 username 을 기반으로 새로운 jwt 생성
    // 클레임 : 토큰에 사용할 정보 조각을 의미, 보통 키-값 형태로 저장
    // setSubject : 클레임에 사용자 정보 저장 (sub)
    // setIssuedAt :  클레임에 토큰 발급시간 저장 (iat)
    // serExpiration : 클레임에 토큰 만료시간 저장 (exp)
    // signWith : 서명과 암호화 방법 지정
    // compact : 서명과 클레임을 조합해서 토큰 생성
    public String generateToken(String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + validity);

        return Jwts.builder().setSubject(username).setIssuedAt(now).setExpiration(expiration).signWith(SignatureAlgorithm.HS256, secretKey).compact();
    }

    // 주어진 JWT 가 유효한지, 토큰의 소유자가 맞는지 확인
    public boolean validateToken(String token, String username) {
        final String tokenUsername = extractUsername(token);

        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }

    // 주어진 JWT 에서 'sub' 클레임(username)을 추출
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    // 주어진 JWT 이 만료되었는지 검사
    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    // 주어진 JWT 내부를 분석하고 검증하여 모든 클레임 정보 추출
    // setSigningKey : 검증키로 서명 설정
    // parseClaimsJws : 파싱 및 서명 검증
    // getBody : 클레임 반환
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }

}
