package spring.jwt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // 서버 응답시 json을 자바스크립트에서 처리할 수 있게 할지 설정
        config.addAllowedOrigin("*");     // 모든 아이피의 응답 허용
        config.addAllowedHeader("*");     // 모든 header 응답 허용
        config.addAllowedMethod("*");     // 모든 Method 요청 하용

        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}
