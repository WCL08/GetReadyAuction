package com.example.getreadyauction.jwt;

import com.example.getreadyauction.entity.ErrorCode;
import com.example.getreadyauction.exception.CustomException;
import com.example.getreadyauction.security.UserDetailsServiceImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    private static final String BEARER_PREFIX = "Bearer ";
    private final UserDetailsServiceImpl userDetailsService;

    @Value("${jwt.secret.key}")
    private String SECURITY_KEY;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256; //HS256 암호화 알고리즘 사용

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(SECURITY_KEY);
        key = Keys.hmacShaKeyFor(bytes);
    }

    // 토큰 생성
    public String createToken(String username) {
        Date date = new Date();
        Date exprTime = Date.from(Instant.now().plus(1, ChronoUnit.HOURS));

        //토큰 앞은 Bearer이 붙음
        //String 형식의 jwt토큰으로 반환됨
        return BEARER_PREFIX +
                Jwts.builder()
                        .signWith(key, signatureAlgorithm) //생성한 key 객체와 key객체를 어떤 알고리즘을 통해 암호화 할건지 지정
                        .setSubject(username) //subject라는 키에 username 넣음
                        .setExpiration(exprTime) //(현재시간 + 1시간)토큰 유효기간 지정
                        .setIssuedAt(date) //언제 토큰이 생성 되었는가
                        .compact();
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 토큰 검증
    public boolean validateToken(String token) throws CustomException {
        try {
            //토큰 검증 (내부적으로 해준다)
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new CustomException(ErrorCode.UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.EMPTY_TOKEN);
        }
    }

    // 토큰에서 사용자 정보 가져오기
    public String getUserInfoFromToken(String token) {
        // token 을 키를 사용해 복호화
        //검증 하고, token 의 payload 에서 getBody().getSubject() 를 통해서 안에 들어있는 정보를 가져옴
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    // 인증 객체 생성
    public Authentication createAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}