package com.example.library.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер страницы "Доступ запрещён".
 *
 * Spring Security перенаправляет сюда пользователя, если у него
 * не хватает роли для запрошенного действия (например, обычный USER
 * пытается открыть форму создания книги, доступную только ADMIN).
 */
@Controller
public class AccessDeniedController {

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}
