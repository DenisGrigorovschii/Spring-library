package com.example.library.service;

import com.example.library.model.User;
import com.example.library.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Сервис для загрузки информации о пользователе для Spring Security
 * 
 * Этот класс реализует интерфейс UserDetailsService, который используется
 * Spring Security для аутентификации пользователей.
 * 
 * Основная задача: преобразовать нашего пользователя (User) из базы данных
 * в объект UserDetails, который понимает Spring Security.
 * 
 * Процесс аутентификации:
 * 1. Пользователь вводит логин и пароль на странице входа
 * 2. Spring Security вызывает loadUserByUsername(username)
 * 3. Мы находим пользователя в базе данных по логину
 * 4. Создаём объект UserDetails с информацией о пользователе
 * 5. Spring Security сравнивает введённый пароль с паролем из БД
 * 6. Если пароли совпадают - пользователь аутентифицирован
 */
@Service  // Указывает Spring, что это сервис (компонент бизнес-логики)
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * Репозиторий для работы с пользователями в базе данных
     * Используется для поиска пользователя по логину
     */
    private final UserRepository userRepository;

    /**
     * Конструктор с внедрением зависимостей
     * 
     * Spring автоматически создаёт и передаёт UserRepository при создании сервиса
     * 
     * @param userRepository репозиторий для работы с пользователями
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Загрузка информации о пользователе по логину
     * 
     * Этот метод вызывается Spring Security при попытке пользователя войти в систему.
     * 
     * Процесс работы:
     * 1. Получаем логин пользователя (username)
     * 2. Ищем пользователя в базе данных по логину
     * 3. Если пользователь не найден - выбрасываем исключение
     * 4. Если найден - создаём объект UserDetails с:
     *    - логином пользователя
     *    - зашифрованным паролем из БД
     *    - ролью пользователя (для контроля доступа)
     * 
     * @param username логин пользователя, который пытается войти
     * @return объект UserDetails с информацией о пользователе
     * @throws UsernameNotFoundException если пользователь с таким логином не найден
     * 
     * ВАЖНО: Пароль должен быть уже зашифрован в базе данных (BCrypt).
     * Spring Security автоматически сравнит введённый пароль с зашифрованным.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Ищем пользователя в базе данных по логину
        // orElseThrow() - если пользователь не найден, выбрасываем исключение
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Создаём объект "роль" (GrantedAuthority) из роли пользователя
        // Spring Security использует роли для контроля доступа к ресурсам
        // Пример: "ROLE_USER", "ROLE_ADMIN"
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());
        
        // Создаём объект UserDetails для Spring Security
        // Это стандартный класс Spring Security, который содержит:
        // - username: логин пользователя
        // - password: зашифрованный пароль из БД
        // - authorities: список ролей пользователя (в нашем случае одна роль)
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),           // Логин пользователя
                user.getPassword(),            // Зашифрованный пароль из БД
                Collections.singleton(authority)  // Роль пользователя (обёрнута в коллекцию)
        );
    }
}


