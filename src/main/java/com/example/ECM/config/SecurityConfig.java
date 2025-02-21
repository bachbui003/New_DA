package com.example.ECM.config;

import com.example.ECM.service.JwtFilter;
import com.example.ECM.service.Impl.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtFilter jwtFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())  // Vô hiệu hóa CSRF nếu bạn đang dùng JWT
                .authorizeHttpRequests(auth -> auth
                        // Các API authentication cho phép tất cả người dùng truy cập
                        .requestMatchers("/api/auth/**").permitAll()

                        // API để xem sản phẩm: ai cũng có thể xem
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                        // API để thêm, sửa, xóa sản phẩm, chỉ ADMIN mới có thể truy cập
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")

                        // API để sửa và xóa người dùng, chỉ ADMIN mới có thể truy cập
                        .requestMatchers(HttpMethod.PUT, "/api/user/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/user/**").hasRole("ADMIN")

                        // ✅ Cho phép USER & ADMIN lấy giỏ hàng của chính họ
                        .requestMatchers("/api/cart/**").hasAnyRole("USER", "ADMIN")  // Chỉ user hoặc admin mới được truy cập
                        .requestMatchers("/api/cart/all").hasRole("ADMIN") // Chỉ admin có thể truy cập

                        // API cho các hành động khác phải được xác thực
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // Không lưu trữ session
                .authenticationProvider(authenticationProvider())  // Cung cấp thông tin xác thực người dùng
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)  // Thêm bộ lọc JWT vào pipeline
                .build();
    }

}
