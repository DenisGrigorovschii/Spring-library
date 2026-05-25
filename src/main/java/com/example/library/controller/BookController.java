package com.example.library.controller;

import com.example.library.model.Book;
import com.example.library.repository.BookRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Контроллер для работы с книгами
 * 
 * Этот контроллер обрабатывает все HTTP запросы, связанные с книгами:
 * - отображение списка книг (с поиском и фильтрацией)
 * - создание новой книги
 * - редактирование существующей книги
 * - просмотр детальной информации о книге
 * - удаление книги
 * 
 * Использует RESTful подход для организации URL:
 * - GET /books - список книг
 * - GET /books/new - форма создания
 * - POST /books - сохранение новой книги
 * - GET /books/{id} - просмотр книги
 * - GET /books/{id}/edit - форма редактирования
 * - POST /books/{id}/delete - удаление книги
 */
@Controller  // Указывает Spring, что это контроллер для обработки веб-запросов
public class BookController {

    /**
     * Репозиторий для работы с книгами в базе данных
     * Предоставляет методы для поиска, сохранения, удаления книг
     */
    private final BookRepository repository;

    /**
     * Конструктор с внедрением зависимостей
     * 
     * Spring автоматически создаёт и передаёт BookRepository при создании контроллера
     * 
     * @param repository репозиторий для работы с книгами
     */
    public BookController(BookRepository repository) {
        this.repository = repository;
    }

    /**
     * Отображение списка всех книг с возможностью поиска и фильтрации
     * 
     * Обрабатывает GET запросы на URL "/" (главная страница) и "/books"
     * 
     * Поддерживает три режима работы:
     * 1. Поиск по запросу - если передан параметр "search"
     * 2. Фильтрация - если передан параметр "genre" или "year"
     * 3. Показать все - если параметры не переданы
     * 
     * @param search поисковый запрос (может быть частью названия, автора или жанра)
     * @param genre жанр для фильтрации (опциональный параметр)
     * @param year год публикации для фильтрации (опциональный параметр)
     * @param model объект Model для передачи данных в шаблон Thymeleaf
     * @return имя шаблона "books/list" (файл: templates/books/list.html)
     * 
     * Примеры URL:
     * - /books - показать все книги
     * - /books?search=война - найти книги со словом "война"
     * - /books?genre=Фантастика - показать книги жанра "Фантастика"
     * - /books?year=2020 - показать книги 2020 года
     * - /books?genre=Классика&year=1869 - комбинированная фильтрация
     */
    @GetMapping({"/", "/books"})  // Обработка GET запросов на / и /books
    public String list(@RequestParam(required = false) String search,  // Опциональный параметр поиска
                       @RequestParam(required = false) String genre,     // Опциональный параметр жанра
                       @RequestParam(required = false) Integer year,      // Опциональный параметр года
                       Model model) {
        try {
            // ========== ЛОГИКА ПОИСКА И ФИЛЬТРАЦИИ ==========
            
            // Режим 1: Поиск по запросу (приоритет выше фильтрации)
            // Проверяем, что параметр search передан и не пустой
            if (search != null && !search.trim().isEmpty()) {
                // Выполняем поиск по названию, автору и жанру
                model.addAttribute("books", repository.searchBooks(search.trim()));
                // Сохраняем поисковый запрос для отображения в форме
                model.addAttribute("searchQuery", search.trim());
            }
            // Режим 2: Фильтрация по жанру и/или году
            else if (genre != null || year != null) {
                // Фильтруем книги по указанным критериям
                model.addAttribute("books", repository.filterBooks(genre, year));
                // Сохраняем выбранные фильтры для отображения в форме
                model.addAttribute("selectedGenre", genre);
                model.addAttribute("selectedYear", year);
            }
            // Режим 3: Показать все книги (если нет параметров поиска/фильтрации)
            else {
                // Получаем все книги из базы данных
                model.addAttribute("books", repository.findAll());
            }
            
            // ========== ДОПОЛНИТЕЛЬНАЯ ИНФОРМАЦИЯ ДЛЯ ШАБЛОНА ==========
            
            // Общее количество книг в базе (для отображения статистики)
            model.addAttribute("totalBooks", repository.count());
            
            // Список всех жанров (для заполнения выпадающего списка фильтров)
            model.addAttribute("genres", repository.findAllGenres());
            
        } catch (Exception e) {
            // ========== ОБРАБОТКА ОШИБОК ==========
            // Если произошла ошибка при работе с БД, показываем пустой список
            // Это предотвращает падение приложения и позволяет пользователю увидеть страницу
            model.addAttribute("books", java.util.Collections.emptyList());
            model.addAttribute("totalBooks", 0L);
            model.addAttribute("genres", java.util.Collections.emptyList());
        }
        
        // Возвращаем имя шаблона для отображения списка книг
        return "books/list";
    }

    /**
     * Отображение формы для создания новой книги
     * 
     * Обрабатывает GET запрос на URL "/books/new"
     * Создаёт пустой объект Book и передаёт его в шаблон формы
     * 
     * @param model объект Model для передачи данных в шаблон
     * @return имя шаблона "books/form" (файл: templates/books/form.html)
     */
    @GetMapping("/books/new")  // Обработка GET запроса на /books/new
    @PreAuthorize("hasRole('ADMIN')")
    public String createForm(Model model) {
        // Создаём новый пустой объект Book для формы
        // Thymeleaf использует этот объект для привязки данных формы
        model.addAttribute("book", new Book());
        return "books/form";
    }

    /**
     * Сохранение новой книги или обновление существующей
     * 
     * Обрабатывает POST запрос на URL "/books"
     * 
     * Логика работы:
     * - Если у книги нет ID (id == null) - создаётся новая книга
     * - Если у книги есть ID - обновляется существующая книга
     * 
     * Выполняет валидацию:
     * - Название книги обязательно
     * - Автор книги обязателен
     * 
     * @param book объект Book, заполненный данными из формы
     * @param ra объект RedirectAttributes для передачи сообщений при перенаправлении
     * @return строка "redirect:/books" - перенаправление на список книг
     * 
     * В случае ошибки валидации возвращает на форму создания/редактирования
     */
    @PostMapping("/books")  // Обработка POST запроса на /books
    @PreAuthorize("hasRole('ADMIN')")
    public String save(@ModelAttribute Book book, RedirectAttributes ra) {
        try {
            // ========== ВАЛИДАЦИЯ ДАННЫХ ==========
            
            // Проверка 1: Название книги обязательно
            if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
                ra.addFlashAttribute("error", "Название книги обязательно");
                // Определяем, куда вернуться: на форму редактирования или создания
                // Если ID есть - редактирование, если нет - создание
                return book.getId() != null ? "redirect:/books/" + book.getId() + "/edit" : "redirect:/books/new";
            }
            
            // Проверка 2: Автор книги обязателен
            if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
                ra.addFlashAttribute("error", "Автор книги обязателен");
                return book.getId() != null ? "redirect:/books/" + book.getId() + "/edit" : "redirect:/books/new";
            }
            
            // ========== СОХРАНЕНИЕ КНИГИ ==========
            
            // Сохраняем книгу в базу данных
            // Если ID = null - создаётся новая запись
            // Если ID != null - обновляется существующая запись
            repository.save(book);
            
            // Сообщение об успехе (разное для создания и обновления)
            ra.addFlashAttribute("success", book.getId() != null ? "Книга обновлена" : "Книга добавлена");
            
            // Перенаправляем на список книг
            return "redirect:/books";
            
        } catch (Exception e) {
            // ========== ОБРАБОТКА ОШИБОК ==========
            // Если произошла ошибка при сохранении (например, проблема с БД)
            ra.addFlashAttribute("error", "Ошибка при сохранении: " + e.getMessage());
            // Возвращаем на форму, чтобы пользователь мог исправить данные
            return book.getId() != null ? "redirect:/books/" + book.getId() + "/edit" : "redirect:/books/new";
        }
    }

    /**
     * Отображение формы для редактирования существующей книги
     * 
     * Обрабатывает GET запрос на URL "/books/{id}/edit"
     * Находит книгу по ID и передаёт её в форму для редактирования
     * 
     * @param id идентификатор книги для редактирования (из URL)
     * @param model объект Model для передачи данных в шаблон
     * @return имя шаблона "books/form" (та же форма, что и для создания)
     * 
     * Если книга с таким ID не найдена, перенаправляет на список книг
     */
    @GetMapping("/books/{id}/edit")  // Обработка GET запроса на /books/{id}/edit
    @PreAuthorize("hasRole('ADMIN')")
    public String editForm(@PathVariable Long id, Model model) {
        // Ищем книгу по ID в базе данных
        // Optional используется для безопасной работы с возможным отсутствием книги
        Optional<Book> b = repository.findById(id);
        
        // Проверяем, найдена ли книга
        if (b.isPresent()) {
            // Если книга найдена, передаём её в модель для формы редактирования
            model.addAttribute("book", b.get());
            return "books/form";  // Используем ту же форму, что и для создания
        }
        
        // Если книга не найдена, перенаправляем на список книг
        return "redirect:/books";
    }

    /**
     * Удаление книги из базы данных
     * 
     * Обрабатывает POST запрос на URL "/books/{id}/delete"
     * Удаляет книгу по указанному ID
     * 
     * @param id идентификатор книги для удаления (из URL)
     * @param ra объект RedirectAttributes для передачи сообщения об успехе
     * @return строка "redirect:/books" - перенаправление на список книг
     * 
     * ВАЖНО: Используется POST, а не DELETE, потому что HTML формы
     * не поддерживают метод DELETE напрямую (только GET и POST)
     */
    @PostMapping("/books/{id}/delete")  // Обработка POST запроса на /books/{id}/delete
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        // Удаляем книгу из базы данных по ID
        repository.deleteById(id);
        
        // Сообщение об успешном удалении
        ra.addFlashAttribute("success", "Книга удалена");
        
        // Перенаправляем на список книг
        return "redirect:/books";
    }

    /**
     * Просмотр детальной информации о книге
     * 
     * Обрабатывает GET запрос на URL "/books/{id}"
     * Находит книгу по ID и отображает её полную информацию
     * 
     * @param id идентификатор книги для просмотра (из URL)
     * @param model объект Model для передачи данных в шаблон
     * @return имя шаблона "books/view" (файл: templates/books/view.html)
     * 
     * Если книга с таким ID не найдена, перенаправляет на список книг
     */
    @GetMapping("/books/{id}")  // Обработка GET запроса на /books/{id}
    public String view(@PathVariable Long id, Model model) {
        // Ищем книгу по ID в базе данных
        Optional<Book> b = repository.findById(id);
        
        // Проверяем, найдена ли книга
        if (b.isPresent()) {
            // Если книга найдена, передаём её в модель для отображения
            model.addAttribute("book", b.get());
            return "books/view";  // Шаблон для просмотра детальной информации
        }
        
        // Если книга не найдена, перенаправляем на список книг
        return "redirect:/books";
    }
}
