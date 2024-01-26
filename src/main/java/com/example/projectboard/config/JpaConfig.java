package com.example.projectboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@EnableJpaAuditing
@Configuration
public class JpaConfig {

  // Auditing 을 통해 생성자와 수정자를 입력할 때 자동으로 입력해주는 내용 설정
  @Bean
  public AuditorAware<String> auditorAware(){
    return () -> Optional.of("minji"); // TODO : 스프링 시큐리티 인증 기능 붙이게 될 때 수정할 것.
  }

}
