package com.example.library.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Глобальные атрибуты для всех Thymeleaf-шаблонов.
 *
 * Добавляет в модель две переменные, доступные на любой странице:
 *   currentUsername - логин текущего пользователя (или null, если не вошёл)
 *   isAdmin         - true, если у пользователя роль ROLE_ADMIN
 *
 * Это даёт возможность в шаблонах писать th:if="${isAdmin}" без
 * подключения сторонних библиотек.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute("currentUsername")
    public String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return auth.getName();
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        for (GrantedAuthority a : auth.getAuthorities()) {
            if ("ROLE_ADMIN".equals(a.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
