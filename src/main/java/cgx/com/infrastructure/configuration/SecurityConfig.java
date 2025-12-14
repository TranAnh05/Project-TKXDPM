package cgx.com.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // Spring Boot 3.x
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Tắt CSRF (vì chúng ta dùng JWT Stateless)
            .csrf(AbstractHttpConfigurer::disable)
            // Cho phép tất cả request (Tạm thời để test API, sau này sẽ chặn sau)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() 
            );
        
        return http.build();
    }
}