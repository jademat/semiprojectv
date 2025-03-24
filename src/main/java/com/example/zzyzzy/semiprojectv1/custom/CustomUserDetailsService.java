package com.example.zzyzzy.semiprojectv1.custom;

import com.example.zzyzzy.semiprojectv1.domain.User;
import com.example.zzyzzy.semiprojectv1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // JPA를 이용해서 사용자 정보를 데이터베이스에서 조회하고 그 결과를 UserDetails에 저장하고 반환
    @Override
    public UserDetails loadUserByUsername(String userid) throws UsernameNotFoundException {

        log.info("loadUserByUsername 호출 : {}",userid);

        // JPA,MariaDB를 이용해서 사용자 정보 확인
        User user = userRepository.findByUserid(userid)
                .orElseThrow(() -> new UsernameNotFoundException("사용자가 존재하지 않습니다."));

        log.info("loadUserByUsername 호출 : {}",user.getPasswd());
        // 인증에 성공하면 userdetails 객체 초기화하고 반환
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUserid())
                .password(user.getPasswd())
                .roles("USER")
                .build();
    }
}
