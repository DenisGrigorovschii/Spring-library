package com.example.library.repository;

import com.example.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Интерфейс BookRepository - репозиторий для работы с книгами
 * 
 * Репозиторий предоставляет методы для работы с книгами в базе данных.
 * Наследуется от JpaRepository, что даёт стандартные CRUD операции:
 * - Create (создание) - save()
 * - Read (чтение) - findById(), findAll()
 * - Update (обновление) - save() (для существующей записи)
 * - Delete (удаление) - deleteById()
 * 
 * Дополнительно содержит кастомные методы для поиска и фильтрации книг.
 * 
 * @param <Book> тип сущности (модель Book)
 * @param <Long> тип первичного ключа (ID книги)
 */
public interface BookRepository extends JpaRepository<Book, Long> {
    
    /**
     * Поиск книг по запросу (поиск в названии, авторе или жанре)
     * 
     * Метод выполняет поиск по трём полям одновременно:
     * - название книги (title)
     * - автор книги (author)
     * - жанр книги (genre)
     * 
     * Поиск нечувствителен к регистру (LOWER) и использует частичное совпадение (LIKE).
     * 
     * @param query поисковый запрос (может быть частью названия, автора или жанра)
     * @return список книг, которые соответствуют запросу
     * 
     * Примеры:
     * - searchBooks("война") найдёт "Война и мир"
     * - searchBooks("толстой") найдёт книги автора "Толстой"
     * - searchBooks("фантастика") найдёт все книги жанра "Фантастика"
     * 
     * SQL эквивалент (примерно):
     * SELECT * FROM book WHERE 
     *   LOWER(title) LIKE '%война%' OR 
     *   LOWER(author) LIKE '%война%' OR 
     *   LOWER(genre) LIKE '%война%'
     */
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(b.genre) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Book> searchBooks(@Param("query") String query);
    
    /**
     * Фильтрация книг по жанру и/или году публикации
     * 
     * Метод позволяет фильтровать книги по одному или обоим критериям:
     * - по жанру (частичное совпадение, нечувствительно к регистру)
     * - по году публикации (точное совпадение)
     * 
     * Если параметр равен null, то фильтр по этому критерию не применяется.
     * 
     * @param genre жанр для фильтрации (может быть null - тогда фильтр не применяется)
     * @param year год публикации для фильтрации (может быть null - тогда фильтр не применяется)
     * @return список отфильтрованных книг
     * 
     * Примеры использования:
     * - filterBooks("Фантастика", null) - все книги жанра "Фантастика"
     * - filterBooks(null, 2020) - все книги 2020 года
     * - filterBooks("Классика", 1869) - классические книги 1869 года
     * - filterBooks(null, null) - все книги (без фильтрации)
     */
    @Query("SELECT b FROM Book b WHERE (:genre IS NULL OR LOWER(b.genre) LIKE LOWER(CONCAT('%', :genre, '%'))) AND (:year IS NULL OR b.year = :year)")
    List<Book> filterBooks(@Param("genre") String genre, @Param("year") Integer year);
    
    /**
     * Получить список всех уникальных жанров книг
     * 
     * Метод возвращает отсортированный список всех жанров, которые есть в базе данных.
     * Используется для заполнения выпадающего списка фильтров на странице.
     * 
     * @return список уникальных жанров, отсортированный по алфавиту
     * 
     * Пример результата: ["Детектив", "Классика", "Фантастика", "Фэнтези"]
     * 
     * DISTINCT - убирает дубликаты (если несколько книг одного жанра)
     * IS NOT NULL - исключает книги без указанного жанра
     * ORDER BY - сортирует по алфавиту
     */
    @Query("SELECT DISTINCT b.genre FROM Book b WHERE b.genre IS NOT NULL ORDER BY b.genre")
    List<String> findAllGenres();
    
    /**
     * Найти книги по точному названию
     * 
     * Spring Data JPA автоматически создаёт реализацию этого метода.
     * Правило именования: findBy + имя поля (с заглавной буквы)
     * 
     * @param title точное название книги для поиска
     * @return список книг с таким названием (обычно одна книга)
     * 
     * ВАЖНО: Поиск по точному совпадению (не частичному).
     * Для частичного поиска используйте searchBooks().
     */
    List<Book> findByTitle(String title);
}

