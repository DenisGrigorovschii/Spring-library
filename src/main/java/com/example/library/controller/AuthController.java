package com.example.library.controller;

import com.example.library.model.User;
import com.example.library.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, RedirectAttributes ra) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            ra.addFlashAttribute("error", "Логин обязателен");
            return "redirect:/register";
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            ra.addFlashAttribute("error", "Пароль обязателен");
            return "redirect:/register";
        }
        
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            ra.addFlashAttribute("error", "Пользователь с таким логином уже существует");
            return "redirect:/register";
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        userRepository.save(user);
        ra.addFlashAttribute("success", "Регистрация успешна! Теперь вы можете войти.");
        return "redirect:/login";
    }
}

