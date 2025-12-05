package com.example.library.controller;

import com.example.library.model.Book;
import com.example.library.repository.BookRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class BookController {

    private final BookRepository repository;

    public BookController(BookRepository repository) {
        this.repository = repository;
    }

    @GetMapping({"/", "/books"})
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) String genre,
                       @RequestParam(required = false) Integer year,
                       Model model) {
        try {
            if (search != null && !search.trim().isEmpty()) {
                model.addAttribute("books", repository.searchBooks(search.trim()));
                model.addAttribute("searchQuery", search.trim());
            } else if (genre != null || year != null) {
                model.addAttribute("books", repository.filterBooks(genre, year));
                model.addAttribute("selectedGenre", genre);
                model.addAttribute("selectedYear", year);
            } else {
                model.addAttribute("books", repository.findAll());
            }
            model.addAttribute("totalBooks", repository.count());
            model.addAttribute("genres", repository.findAllGenres());
        } catch (Exception e) {
            model.addAttribute("books", java.util.Collections.emptyList());
            model.addAttribute("totalBooks", 0L);
            model.addAttribute("genres", java.util.Collections.emptyList());
        }
        return "books/list";
    }

    @GetMapping("/books/new")
    public String createForm(Model model) {
        model.addAttribute("book", new Book());
        return "books/form";
    }

    @PostMapping("/books")
    public String save(@ModelAttribute Book book, RedirectAttributes ra) {
        try {
            if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
                ra.addFlashAttribute("error", "Название книги обязательно");
                return book.getId() != null ? "redirect:/books/" + book.getId() + "/edit" : "redirect:/books/new";
            }
            if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
                ra.addFlashAttribute("error", "Автор книги обязателен");
                return book.getId() != null ? "redirect:/books/" + book.getId() + "/edit" : "redirect:/books/new";
            }
            
            repository.save(book);
            ra.addFlashAttribute("success", book.getId() != null ? "Книга обновлена" : "Книга добавлена");
            return "redirect:/books";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Ошибка при сохранении: " + e.getMessage());
            return book.getId() != null ? "redirect:/books/" + book.getId() + "/edit" : "redirect:/books/new";
        }
    }

    @GetMapping("/books/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Optional<Book> b = repository.findById(id);
        if (b.isPresent()) {
            model.addAttribute("book", b.get());
            return "books/form";
        }
        return "redirect:/books";
    }

    @PostMapping("/books/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        repository.deleteById(id);
        ra.addFlashAttribute("success", "Книга удалена");
        return "redirect:/books";
    }

    @GetMapping("/books/{id}")
    public String view(@PathVariable Long id, Model model) {
        Optional<Book> b = repository.findById(id);
        if (b.isPresent()) {
            model.addAttribute("book", b.get());
            return "books/view";
        }
        return "redirect:/books";
    }
}

