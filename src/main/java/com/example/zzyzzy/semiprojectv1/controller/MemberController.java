package com.example.zzyzzy.semiprojectv1.controller;

import com.example.zzyzzy.semiprojectv1.domain.Member;
import com.example.zzyzzy.semiprojectv1.domain.MemberDTO;
import com.example.zzyzzy.semiprojectv1.jwt.JwtTokenProvider;
import com.example.zzyzzy.semiprojectv1.repository.MemberRepository;
import com.example.zzyzzy.semiprojectv1.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @GetMapping("/join")
    public String join() {
        return "views/member/join";
    }

    // ResponseEntity는 스프링에서 HTTP와 관련된 기능을 구현할때 사용
    // 상태코드, HTTP헤더, HTTP본문등을 명시적으로 설정 가능
    @PostMapping("/join")
    public ResponseEntity<?> joinok(MemberDTO member) {
        // 회원 가입 처리시 기타오류 발생에 대한 응답코드 설정
        ResponseEntity<?> response = ResponseEntity.internalServerError().build();

        log.info("submit된 회원 정보 : {}", member);

        try {
            // 정상 처리시 상태코드 200으로 응답
            memberService.newMember(member);
            response = ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            // 비정상 처리시 상태코드 400으로 응답 - 클라이언트 잘못
            // 중복 아이디나 중복 이메일 사용시
            response = ResponseEntity.badRequest().body(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // 비정상 처리시 상태코드 500으로 응답 - 서버 잘못
            e.printStackTrace();
        }

        return response;
    }

    @GetMapping("/login")
    public String login() {
        return "views/member/login";
    }

     //스프링 시큐리티가 자동으로 처리 - 생략
    @PostMapping("/login")
    public ResponseEntity<?> loginok(MemberDTO member, HttpServletResponse res) {
        // 로그인 처리시 기타오류 발생에 대한 응답코드 설정
        ResponseEntity<?> response = ResponseEntity.internalServerError().body("서버처리시 오류 발생");

        log.info("submit된 로그인 정보 : {}", member);

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(member.getUserid(),member.getPasswd()));

            // 인증이 완료되면 jwt 토큰 생성
            final String jwt = jwtTokenProvider.generateToken(member.getUserid());

            // JWT 토큰을 쿠키에 저장
            Cookie cookie = new Cookie("jwt", jwt);
            cookie.setHttpOnly(true);               // 토큰은 header를 통해서만 서버로 전송 가능
            cookie.setMaxAge(60 * 30);              // 30분
            cookie.setPath("/");
            res.addCookie(cookie);


            response = ResponseEntity.ok().body("로그인 성공");
        } catch (BadCredentialsException e) {
            // 비정상 처리시 상태코드 401으로 응답 - 아이디나 비밀번호 잘못 입력시
            response = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디나 비밀번호를 확인해주세요");

            log.info(e.getMessage());
        } catch (Exception e) {
            // 비정상 처리시 상태코드 500으로 응답 - 서버 잘못
            e.printStackTrace();
        }

        return response;
    }
    
    @GetMapping("/myinfo")
    public String myinfo(Authentication authentication, Model model) {
        String returnUrl = "redirect:/member/login";

        if(authentication != null && authentication.isAuthenticated()){
            // 인증 완료된 사용자 정보(아이디)를 가져옴
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            // 그외 사용자 정보(이름,이메일,가입일) 를 가져오기 위해 다시 사용자 테이블 조회
            model.addAttribute("loginUser", memberService.findbyUserid(userDetails));
            returnUrl = "views/member/myinfo";
        }

        return returnUrl;
    }

// 스프링 시큐리티가 자동으로 처리 - 생략
    @GetMapping("/logout")
    public String logout(HttpServletResponse res, HttpServletRequest req) {
        // 쿠키에서 JWT 삭제
        Cookie cookie = new Cookie("jwt", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        res.addCookie(cookie);

        // 세션 무효화

        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }

    @GetMapping("/loginfail")
    public String loginfail() {
        return "views/member/loginfail";
    }
}
