package com.example.library.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Обработка выхода по GET-запросу.
 *
 * Spring Security по умолчанию ожидает logout через POST, но пользователь
 * может попасть на /logout обычной ссылкой или вручную из адресной строки.
 * Этот контроллер завершает сессию и перенаправляет на страницу входа.
 */
@Controller
public class LogoutController {

    @GetMapping("/logout")
    public String logout(HttpServletRequest request,
                         HttpServletResponse response,
                         Authentication authentication) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return "redirect:/login?logout";
    }
}
