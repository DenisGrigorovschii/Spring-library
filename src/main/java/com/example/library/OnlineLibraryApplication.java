package com.example.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс приложения "Онлайн библиотека"
 * 
 * Этот класс является точкой входа в Spring Boot приложение.
 * Аннотация @SpringBootApplication включает в себя:
 * - @Configuration - указывает, что класс содержит конфигурацию Spring
 * - @EnableAutoConfiguration - включает автоматическую конфигурацию Spring Boot
 * - @ComponentScan - включает сканирование компонентов в пакете и подпакетах
 */
@SpringBootApplication
public class OnlineLibraryApplication {
    
    /**
     * Главный метод приложения - точка входа в программу
     * 
     * @param args аргументы командной строки, переданные при запуске приложения
     * 
     * Метод запускает Spring Boot приложение, которое:
     * 1. Инициализирует контекст Spring
     * 2. Загружает все бины (компоненты, сервисы, контроллеры)
     * 3. Настраивает встроенный веб-сервер (Tomcat)
     * 4. Запускает приложение на порту, указанном в application.properties
     */
    public static void main(String[] args) {
        // Запуск Spring Boot приложения
        // SpringApplication.run() создаёт ApplicationContext и запускает встроенный сервер
        SpringApplication.run(OnlineLibraryApplication.class, args);
    }
}
