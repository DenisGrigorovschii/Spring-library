package com.example.library.model;

import jakarta.persistence.*;

/**
 * Класс User - модель данных пользователя системы
 * 
 * Этот класс представляет сущность пользователя в базе данных.
 * Используется для хранения информации о пользователях библиотеки:
 * - идентификатор пользователя
 * - логин (уникальный)
 * - пароль (хранится в зашифрованном виде)
 * - роль пользователя (ROLE_USER, ROLE_ADMIN и т.д.)
 * 
 * Класс использует JPA (Java Persistence API) для работы с базой данных.
 */
@Entity  // Указывает, что это JPA сущность (таблица в БД)
@Table(name = "users")  // Имя таблицы в базе данных
public class User {
    
    /**
     * Уникальный идентификатор пользователя
     * Генерируется автоматически базой данных при создании записи
     */
    @Id  // Указывает, что это первичный ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Автоматическая генерация ID (AUTO_INCREMENT)
    private Long id;

    /**
     * Логин пользователя
     * Должен быть уникальным (не может быть двух пользователей с одинаковым логином)
     */
    @Column(unique = true)  // Ограничение уникальности на уровне БД
    private String username;
    
    /**
     * Пароль пользователя
     * ВАЖНО: В реальных приложениях пароль должен храниться в зашифрованном виде (BCrypt)
     * и никогда не должен быть в открытом виде в базе данных
     */
    private String password;
    
    /**
     * Роль пользователя в системе
     * Примеры: "ROLE_USER" (обычный пользователь), "ROLE_ADMIN" (администратор)
     * Используется Spring Security для контроля доступа к ресурсам
     */
    private String role;

    /**
     * Конструктор по умолчанию (без параметров)
     * 
     * Необходим для JPA - Hibernate использует его при создании объектов из БД
     */
    public User() {}

    /**
     * Конструктор с параметрами для создания нового пользователя
     * 
     * @param username логин пользователя
     * @param password пароль пользователя (должен быть уже зашифрован)
     * @param role роль пользователя в системе
     */
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // ========== Геттеры и сеттеры ==========
    // Геттеры - методы для получения значений полей
    // Сеттеры - методы для установки значений полей
    
    /**
     * Получить идентификатор пользователя
     * @return уникальный ID пользователя
     */
    public Long getId() { return id; }
    
    /**
     * Установить идентификатор пользователя
     * @param id новый ID пользователя
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Получить логин пользователя
     * @return логин пользователя
     */
    public String getUsername() { return username; }
    
    /**
     * Установить логин пользователя
     * @param username новый логин
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * Получить пароль пользователя
     * ВАЖНО: В реальных приложениях этот метод не должен возвращать пароль напрямую
     * @return пароль (обычно в зашифрованном виде)
     */
    public String getPassword() { return password; }
    
    /**
     * Установить пароль пользователя
     * @param password новый пароль (должен быть зашифрован перед сохранением)
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * Получить роль пользователя
     * @return роль пользователя (например, "ROLE_USER")
     */
    public String getRole() { return role; }
    
    /**
     * Установить роль пользователя
     * @param role новая роль пользователя
     */
    public void setRole(String role) { this.role = role; }
}


