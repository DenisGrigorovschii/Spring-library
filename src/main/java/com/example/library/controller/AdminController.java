package com.example.library.controller;

import com.example.library.model.User;
import com.example.library.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Админ-панель: список пользователей и управление их ролями.
 *
 * Доступна только пользователям с ролью ROLE_ADMIN. Защита продублирована:
 *  - на уровне URL в SecurityConfig (/admin/**),
 *  - на уровне метода через @PreAuthorize.
 *
 * Поддерживает:
 *  - просмотр всех зарегистрированных пользователей,
 *  - переключение роли USER <-> ADMIN,
 *  - удаление пользователя (кроме самого себя — чтобы админ не "залочил" систему).
 */
@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/admin/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    @PostMapping("/admin/users/{id}/toggle-role")
    public String toggleRole(@PathVariable Long id,
                             Authentication auth,
                             RedirectAttributes ra) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Пользователь не найден");
            return "redirect:/admin/users";
        }
        User user = opt.get();

        // Запрещаем менять роль самому себе, чтобы случайно не лишиться прав
        if (user.getUsername().equals(auth.getName())) {
            ra.addFlashAttribute("error", "Нельзя менять роль самому себе");
            return "redirect:/admin/users";
        }

        String newRole = "ROLE_ADMIN".equals(user.getRole()) ? "ROLE_USER" : "ROLE_ADMIN";
        user.setRole(newRole);
        userRepository.save(user);
        ra.addFlashAttribute("success",
                "Роль пользователя " + user.getUsername() + " изменена на " + newRole);
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             Authentication auth,
                             RedirectAttributes ra) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Пользователь не найден");
            return "redirect:/admin/users";
        }
        User user = opt.get();

        if (user.getUsername().equals(auth.getName())) {
            ra.addFlashAttribute("error", "Нельзя удалить самого себя");
            return "redirect:/admin/users";
        }

        userRepository.deleteById(id);
        ra.addFlashAttribute("success", "Пользователь " + user.getUsername() + " удалён");
        return "redirect:/admin/users";
    }
}
