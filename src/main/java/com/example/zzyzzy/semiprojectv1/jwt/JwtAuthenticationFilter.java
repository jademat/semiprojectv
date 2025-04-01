package com.example.zzyzzy.semiprojectv1.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    // 클라이언트로부터 HTTP 요청이 들어오면 JwtAuthenticationFilter 의 doFilterInternal 메서드가 호출
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain fc) throws ServletException, IOException {

        log.info(" >> JWT Authentication Filter 호출");

        String jwt = null;
        String username = null;

        // 1. 요청으로부터 JWT 토큰 확인
        // 1a. 요청헤더에서 JWT 토큰 확인
        log.info(">> check token in http header");
        final String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        }
        // 1b. 쿠키에서 JWT 토큰 확인
        log.info(">> check cookie in http header");
        if (jwt == null) {  // header 에서 jwt를 찾지 못했다면
            Cookie[] cookies = req.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("jwt")) {
                        jwt = cookie.getValue();
                        break;
                    }
                }
            }
        }

        // 2. 토큰 내부의 사용자 이름 추출
        log.info(">> get username in jwt : {}",jwt);
        if (jwt != null) {
            username = jwtTokenProvider.extractUsername(jwt);
        }
        log.info(">> get username : {}", username);

        // 3. 인증정보 확인 및 검증
        // 요청 정보에 대해 아직 아무런 인증이 되징 않았다면 (중복인증 방지)
        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 추출된 username을 이용해서 사용자 정보를 디비에서 조회
            log.info(">> JwtAuthenticationFilter - loadUserByUsername 호출 ");
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 가져온 토큰이 유효한지와 토큰에 포함된 이름이 userDetails의 사용자이름과 동일한지 검사
            if(jwtTokenProvider.validateToken(jwt,userDetails.getUsername())){
                // 인증된 사용자 주체를
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

                // 생성된 인증객체를 SecurityContextHolder의 현재 컨텍스트에 저장
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        // 현재 요청을 필터 체인의 다음 필터로 전달
        fc.doFilter(req, res);
    }
}
