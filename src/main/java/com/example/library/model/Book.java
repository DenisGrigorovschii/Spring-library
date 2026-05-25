package com.example.library.model;

import jakarta.persistence.*;

/**
 * Класс Book - модель данных книги в библиотеке
 * 
 * Этот класс представляет сущность книги в базе данных.
 * Хранит информацию о книге:
 * - уникальный идентификатор
 * - название книги
 * - автор книги
 * - жанр книги
 * - год публикации
 * - описание книги (может быть длинным текстом)
 * 
 * Класс использует JPA для автоматического создания таблицы в БД
 * и работы с данными через репозитории.
 */
@Entity  // Указывает, что это JPA сущность
@Table(name = "book")  // Имя таблицы в базе данных
public class Book {
    
    /**
     * Уникальный идентификатор книги
     * Автоматически генерируется базой данных при добавлении новой книги
     */
    @Id  // Первичный ключ таблицы
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // AUTO_INCREMENT в БД
    private Long id;

    /**
     * Название книги
     * Пример: "Война и мир", "1984", "Гарри Поттер"
     */
    private String title;
    
    /**
     * Автор книги
     * Пример: "Лев Толстой", "Джордж Оруэлл", "Джоан Роулинг"
     */
    private String author;
    
    /**
     * Жанр книги
     * Пример: "Классика", "Фантастика", "Фэнтези", "Детектив"
     */
    private String genre;
    
    /**
     * Год публикации книги
     * Пример: 1869, 1949, 1997
     * 
     * Используется Integer вместо int, чтобы можно было хранить null
     * (если год публикации неизвестен)
     */
    @Column(name = "publication_year")  // Имя колонки в БД отличается от имени поля
    private Integer year;
    
    /**
     * Описание книги
     * Может содержать краткое содержание, аннотацию или другую информацию о книге
     * 
     * Ограничение длины до 2000 символов для оптимизации хранения в БД
     */
    @Column(length = 2000)  // Максимальная длина текста в БД
    private String description;

    /**
     * Конструктор по умолчанию
     * 
     * Необходим для JPA - Hibernate использует его при загрузке данных из БД
     */
    public Book() {}

    /**
     * Конструктор с параметрами для создания новой книги
     * 
     * @param title название книги
     * @param author автор книги
     * @param genre жанр книги
     * @param year год публикации
     * @param description описание книги
     */
    public Book(String title, String author, String genre, Integer year, String description) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.year = year;
        this.description = description;
    }

    // ========== Геттеры и сеттеры ==========
    // Методы для доступа к полям класса
    
    /**
     * Получить идентификатор книги
     * @return уникальный ID книги
     */
    public Long getId() { return id; }
    
    /**
     * Установить идентификатор книги
     * @param id новый ID книги
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Получить название книги
     * @return название книги
     */
    public String getTitle() { return title; }
    
    /**
     * Установить название книги
     * @param title новое название
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * Получить автора книги
     * @return имя автора
     */
    public String getAuthor() { return author; }
    
    /**
     * Установить автора книги
     * @param author имя автора
     */
    public void setAuthor(String author) { this.author = author; }

    /**
     * Получить жанр книги
     * @return жанр книги
     */
    public String getGenre() { return genre; }
    
    /**
     * Установить жанр книги
     * @param genre новый жанр
     */
    public void setGenre(String genre) { this.genre = genre; }

    /**
     * Получить год публикации
     * @return год публикации (может быть null)
     */
    public Integer getYear() { return year; }
    
    /**
     * Установить год публикации
     * @param year год публикации (может быть null)
     */
    public void setYear(Integer year) { this.year = year; }

    /**
     * Получить описание книги
     * @return описание книги
     */
    public String getDescription() { return description; }
    
    /**
     * Установить описание книги
     * @param description новое описание (максимум 2000 символов)
     */
    public void setDescription(String description) { this.description = description; }
}

