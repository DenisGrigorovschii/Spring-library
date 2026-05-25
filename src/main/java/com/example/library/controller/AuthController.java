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

/**
 * Контроллер аутентификации и регистрации пользователей
 * 
 * Этот контроллер обрабатывает HTTP запросы, связанные с:
 * - отображением страницы входа (login)
 * - отображением страницы регистрации
 * - обработкой регистрации нового пользователя
 * 
 * Использует Spring MVC для обработки веб-запросов и Thymeleaf для рендеринга HTML.
 */
@Controller  // Указывает Spring, что это контроллер для обработки веб-запросов
public class AuthController {

    /**
     * Репозиторий для работы с пользователями в базе данных
     * Позволяет сохранять, искать и получать данные о пользователях
     */
    private final UserRepository userRepository;
    
    /**
     * Кодировщик паролей (BCrypt)
     * Используется для безопасного хранения паролей в зашифрованном виде
     * Пароли никогда не хранятся в открытом виде!
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Конструктор с внедрением зависимостей (Dependency Injection)
     * 
     * Spring автоматически создаёт и передаёт объекты UserRepository и PasswordEncoder
     * при создании контроллера. Это называется "внедрение зависимостей".
     * 
     * @param userRepository репозиторий для работы с пользователями
     * @param passwordEncoder кодировщик паролей
     */
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Отображение страницы входа в систему
     * 
     * Обрабатывает GET запрос на URL "/login"
     * Возвращает имя шаблона Thymeleaf для рендеринга страницы входа
     * 
     * @return имя шаблона "auth/login" (файл: templates/auth/login.html)
     * 
     * Примечание: Сама аутентификация (проверка логина/пароля) обрабатывается
     * Spring Security автоматически на основе настроек в SecurityConfig
     */
    @GetMapping("/login")  // Обработка GET запроса на /login
    public String login() {
        // Возвращаем имя шаблона Thymeleaf
        // Spring найдёт файл templates/auth/login.html и отобразит его
        return "auth/login";
    }

    /**
     * Отображение формы регистрации нового пользователя
     * 
     * Обрабатывает GET запрос на URL "/register"
     * Создаёт пустой объект User и передаёт его в шаблон для заполнения формы
     * 
     * @param model объект Model для передачи данных в шаблон Thymeleaf
     * @return имя шаблона "auth/register" (файл: templates/auth/register.html)
     */
    @GetMapping("/register")  // Обработка GET запроса на /register
    public String registerForm(Model model) {
        // Создаём новый пустой объект User для формы регистрации
        // Thymeleaf использует этот объект для привязки данных формы (th:object)
        model.addAttribute("user", new User());
        return "auth/register";
    }

    /**
     * Обработка регистрации нового пользователя
     * 
     * Обрабатывает POST запрос на URL "/register"
     * Выполняет валидацию данных и сохраняет нового пользователя в базу данных
     * 
     * Процесс регистрации:
     * 1. Проверка, что логин не пустой
     * 2. Проверка, что пароль не пустой
     * 3. Проверка, что пользователь с таким логином ещё не существует
     * 4. Шифрование пароля с помощью BCrypt
     * 5. Установка роли "ROLE_USER" (обычный пользователь)
     * 6. Сохранение пользователя в базу данных
     * 7. Перенаправление на страницу входа с сообщением об успехе
     * 
     * @param user объект User, заполненный данными из формы регистрации
     * @param ra объект RedirectAttributes для передачи сообщений при перенаправлении
     * @return строка "redirect:/login" - перенаправление на страницу входа
     * 
     * В случае ошибки возвращает "redirect:/register" с сообщением об ошибке
     */
    @PostMapping("/register")  // Обработка POST запроса на /register
    public String register(@ModelAttribute User user, RedirectAttributes ra) {
        // ========== ВАЛИДАЦИЯ ДАННЫХ ==========
        
        // Проверка 1: Логин обязателен
        // trim() убирает пробелы в начале и конце строки
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            // addFlashAttribute - сообщение будет доступно только один раз (после перенаправления)
            ra.addFlashAttribute("error", "Логин обязателен");
            return "redirect:/register";  // Возвращаем на форму регистрации
        }
        
        // Проверка 2: Пароль обязателен
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            ra.addFlashAttribute("error", "Пароль обязателен");
            return "redirect:/register";
        }
        
        // Проверка 3: Пользователь с таким логином не должен существовать
        // isPresent() проверяет, найден ли пользователь в базе
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            ra.addFlashAttribute("error", "Пользователь с таким логином уже существует");
            return "redirect:/register";
        }
        
        // ========== СОХРАНЕНИЕ ПОЛЬЗОВАТЕЛЯ ==========
        
        // ВАЖНО: Шифруем пароль перед сохранением!
        // passwordEncoder.encode() использует алгоритм BCrypt для создания хеша пароля
        // Один и тот же пароль будет иметь разный хеш при каждом шифровании (из-за "соли")
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Устанавливаем роль нового пользователя как обычного пользователя
        // ROLE_USER - стандартная роль для зарегистрированных пользователей
        user.setRole("ROLE_USER");
        
        // Сохраняем пользователя в базу данных
        // save() создаёт новую запись, если ID = null, или обновляет существующую
        userRepository.save(user);
        
        // Сообщение об успешной регистрации
        ra.addFlashAttribute("success", "Регистрация успешна! Теперь вы можете войти.");
        
        // Перенаправляем на страницу входа
        return "redirect:/login";
    }
}

