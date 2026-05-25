package com.example.library.security;

import com.example.library.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация безопасности Spring Security.
 *
 * Здесь реализованы:
 * - аутентификация (форма входа /login)
 * - регистрация открыта всем (/register)
 * - logout (/logout)
 * - хеширование паролей (BCrypt)
 * - role-based авторизация:
 *     ROLE_USER  - может просматривать каталог и карточки книг
 *     ROLE_ADMIN - дополнительно может создавать, редактировать и удалять книги
 * - страница "доступ запрещён" (/access-denied) для попыток обойти роли
 * - защита на уровне методов (@PreAuthorize в контроллерах) благодаря @EnableMethodSecurity
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // включает работу @PreAuthorize/@PostAuthorize на методах
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userDetailsService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())

            .authorizeHttpRequests(auth -> auth
                // публично доступные ресурсы и страницы
                .requestMatchers("/css/**", "/login", "/register", "/logout", "/access-denied").permitAll()

                // вся админ-панель — только ROLE_ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // действия с книгами, доступные ТОЛЬКО администратору
                .requestMatchers(
                        "/books/new",
                        "/books/*/edit",
                        "/books/*/delete"
                ).hasRole("ADMIN")

                // только админ может отправлять POST на /books (создание/обновление)
                .requestMatchers(
                        org.springframework.http.HttpMethod.POST, "/books"
                ).hasRole("ADMIN")

                // просмотр каталога и карточек книг — для любой авторизованной роли
                .requestMatchers("/", "/books", "/books/*").hasAnyRole("USER", "ADMIN")

                // всё остальное — только для авторизованных пользователей
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/books", true)
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // куда отправить пользователя, если у него не хватает роли
            .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"));

        return http.build();
    }
}
