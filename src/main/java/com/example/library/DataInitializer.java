package com.example.library;

import com.example.library.model.Book;
import com.example.library.model.User;
import com.example.library.repository.BookRepository;
import com.example.library.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DataInitializer {

    @Bean
    @Transactional
    public CommandLineRunner initData(BookRepository bookRepository, UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            try {
                Thread.sleep(3000);
                
                // Создание пользователя
                try {
                    if (userRepository.findByUsername("user").isEmpty()) {
                        userRepository.save(new User("user", encoder.encode("password"), "ROLE_USER"));
                        System.out.println("Пользователь 'user' создан");
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка при создании пользователя: " + e.getMessage());
                    e.printStackTrace();
                }
                
                // Создание тестовых книг (10 штук)
                try {
                    long count = bookRepository.count();
                    System.out.println("Текущее количество книг в БД: " + count);
                    
                    if (count < 10) {
                        // Список всех 10 книг
                        Book[] books = {
                            new Book("Тени над Арктуром", "Владислав Кравец", "Фантастика", 2020, "В отдалённой научной станции на планете Арктур учёные находят артефакт древней цивилизации. Но с его пробуждением начинают исчезать люди."),
                            new Book("Песнь о стеклянных башнях", "Алина Рубан", "Фантастика", 2021, "В будущем мегаполисе девушка-программист обнаруживает забытый «код сочувствия». Этот код способен вернуть людям чувства."),
                            new Book("Северный хронотоп", "Дмитрий Мельник", "Приключения", 2022, "Экспедиция в Арктику находит часы, способные останавливать время. С каждым использованием они крадут воспоминания владельца."),
                            new Book("Кафе на перекрёстке миров", "Екатерина Верес", "Фэнтези", 2023, "Маленькое кафе в центре города оказывается порталом между мирами. Посетители — не люди, а существа из снов и воспоминаний."),
                            new Book("Пепельные письма", "Олег Миронов", "Постапокалипсис", 2024, "После апокалипсиса выжившие находят ящик с письмами, написанными людьми из прошлого. Каждое письмо меняет судьбу получателя."),
                            new Book("Мастер и Маргарита", "Михаил Булгаков", "Классика", 1967, "Философский роман о дьяволе, посетившем Москву 1930-х годов, и о любви Мастера и Маргариты."),
                            new Book("1984", "Джордж Оруэлл", "Антиутопия", 1949, "Роман-антиутопия о тоталитарном обществе, где за каждым следят, а свобода слова запрещена."),
                            new Book("Война и мир", "Лев Толстой", "Классика", 1869, "Эпический роман о русском обществе во время наполеоновских войн. Одна из величайших книг мировой литературы."),
                            new Book("Гарри Поттер и философский камень", "Джоан Роулинг", "Фэнтези", 1997, "Первая книга о юном волшебнике Гарри Поттере и его приключениях в школе магии Хогвартс."),
                            new Book("Преступление и наказание", "Фёдор Достоевский", "Классика", 1866, "Психологический роман о студенте Раскольникове, который решает убить старуху-процентщицу ради идеи.")
                        };
                        
                        // Добавляем только недостающие книги
                        int added = 0;
                        for (int i = (int)count; i < 10 && i < books.length; i++) {
                            try {
                                bookRepository.save(books[i]);
                                added++;
                                System.out.println("Добавлена книга: " + books[i].getTitle());
                            } catch (Exception ex) {
                                System.err.println("Ошибка при добавлении книги " + books[i].getTitle() + ": " + ex.getMessage());
                            }
                        }
                        System.out.println("Всего добавлено книг: " + added);
                    } else {
                        System.out.println("В базе уже есть 10 или больше книг");
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка при создании книг: " + e.getMessage());
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }
}

