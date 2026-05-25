package com.example.library.controller;

import com.example.library.model.Book;
import com.example.library.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты для BookController.
 * Репозиторий замокирован, реальной БД не нужно.
 */
@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookRepository repository;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private BookController bookController;

    private Book sampleBook;

    @BeforeEach
    void setUp() {
        sampleBook = new Book("1984", "Оруэлл", "Антиутопия", 1949, "Описание");
        sampleBook.setId(1L);
    }

    @Test
    void list_shouldReturnAllBooks_whenNoFilters() {
        when(repository.findAll()).thenReturn(List.of(sampleBook));
        when(repository.count()).thenReturn(1L);
        when(repository.findAllGenres()).thenReturn(List.of("Антиутопия"));

        Model model = new ExtendedModelMap();
        String view = bookController.list(null, null, null, model);

        assertThat(view).isEqualTo("books/list");
        assertThat(model.getAttribute("books")).isEqualTo(List.of(sampleBook));
        assertThat(model.getAttribute("totalBooks")).isEqualTo(1L);
        verify(repository, never()).searchBooks(anyString());
    }

    @Test
    void list_shouldUseSearch_whenSearchProvided() {
        when(repository.searchBooks("война")).thenReturn(List.of(sampleBook));
        when(repository.count()).thenReturn(1L);
        when(repository.findAllGenres()).thenReturn(List.of());

        Model model = new ExtendedModelMap();
        String view = bookController.list("война", null, null, model);

        assertThat(view).isEqualTo("books/list");
        assertThat(model.getAttribute("searchQuery")).isEqualTo("война");
        verify(repository).searchBooks("война");
        verify(repository, never()).findAll();
    }

    @Test
    void list_shouldUseFilter_whenGenreOrYearProvided() {
        when(repository.filterBooks("Классика", 1869)).thenReturn(List.of(sampleBook));
        when(repository.count()).thenReturn(1L);
        when(repository.findAllGenres()).thenReturn(List.of("Классика"));

        Model model = new ExtendedModelMap();
        String view = bookController.list(null, "Классика", 1869, model);

        assertThat(view).isEqualTo("books/list");
        assertThat(model.getAttribute("selectedGenre")).isEqualTo("Классика");
        assertThat(model.getAttribute("selectedYear")).isEqualTo(1869);
        verify(repository).filterBooks("Классика", 1869);
    }

    @Test
    void save_shouldRejectWhenTitleEmpty() {
        Book book = new Book();
        book.setTitle(" ");
        book.setAuthor("Автор");

        String view = bookController.save(book, redirectAttributes);

        verify(repository, never()).save(any());
        verify(redirectAttributes).addFlashAttribute(eq("error"), any());
        assertThat(view).isEqualTo("redirect:/books/new");
    }

    @Test
    void save_shouldRejectWhenAuthorEmpty() {
        Book book = new Book();
        book.setTitle("Название");
        book.setAuthor("");

        String view = bookController.save(book, redirectAttributes);

        verify(repository, never()).save(any());
        verify(redirectAttributes).addFlashAttribute(eq("error"), any());
        assertThat(view).isEqualTo("redirect:/books/new");
    }

    @Test
    void save_shouldPersistAndRedirect_whenValid() {
        Book book = new Book("Новая", "Автор", "Жанр", 2024, "desc");

        String view = bookController.save(book, redirectAttributes);

        verify(repository).save(book);
        verify(redirectAttributes).addFlashAttribute(eq("success"), any());
        assertThat(view).isEqualTo("redirect:/books");
    }

    @Test
    void view_shouldReturnViewTemplate_whenBookExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleBook));

        Model model = new ExtendedModelMap();
        String view = bookController.view(1L, model);

        assertThat(view).isEqualTo("books/view");
        assertThat(model.getAttribute("book")).isEqualTo(sampleBook);
    }

    @Test
    void view_shouldRedirect_whenBookNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        String view = bookController.view(99L, new ExtendedModelMap());

        assertThat(view).isEqualTo("redirect:/books");
    }

    @Test
    void delete_shouldCallRepositoryAndRedirect() {
        String view = bookController.delete(5L, redirectAttributes);

        verify(repository).deleteById(5L);
        verify(redirectAttributes).addFlashAttribute(eq("success"), any());
        assertThat(view).isEqualTo("redirect:/books");
    }
}
