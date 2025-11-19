package cgx.com.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cấu hình Spring Security.
 * Mục đích: Cho phép truy cập công khai (không cần token) vào các API Đăng ký/Đăng nhập.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // Tắt CSRF để test Postman dễ dàng
            .authorizeHttpRequests(auth -> auth
                // Cho phép TẤT CẢ request truy cập không cần Spring Security can thiệp
                // (Vì chúng ta đã tự check token trong Use Case rồi)
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
