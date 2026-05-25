package com.example.library.controller;

import com.example.library.model.User;
import com.example.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты для AuthController (регистрация пользователя).
 * Поднимаем только сам контроллер, все его зависимости замокированы.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AuthController authController;

    private User newUser;

    @BeforeEach
    void setUp() {
        newUser = new User();
        newUser.setUsername("john");
        newUser.setPassword("plain-password");
    }

    @Test
    void register_shouldEncodePasswordAndSetRoleUser() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain-password")).thenReturn("HASHED");

        String view = authController.register(newUser, redirectAttributes);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("john");
        assertThat(saved.getPassword()).isEqualTo("HASHED");          // пароль захэширован
        assertThat(saved.getRole()).isEqualTo("ROLE_USER");           // роль по умолчанию
        assertThat(view).isEqualTo("redirect:/login");
    }

    @Test
    void register_shouldFail_whenUsernameAlreadyExists() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(new User()));

        String view = authController.register(newUser, redirectAttributes);

        verify(userRepository, never()).save(any());                  // в БД ничего не пишем
        verify(redirectAttributes).addFlashAttribute(eq("error"), any());
        assertThat(view).isEqualTo("redirect:/register");
    }

    @Test
    void register_shouldFail_whenUsernameIsBlank() {
        newUser.setUsername("   ");

        String view = authController.register(newUser, redirectAttributes);

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
        verify(redirectAttributes).addFlashAttribute(eq("error"), any());
        assertThat(view).isEqualTo("redirect:/register");
    }

    @Test
    void register_shouldFail_whenPasswordIsBlank() {
        newUser.setPassword("");

        String view = authController.register(newUser, redirectAttributes);

        verify(userRepository, never()).save(any());
        verify(redirectAttributes).addFlashAttribute(eq("error"), any());
        assertThat(view).isEqualTo("redirect:/register");
    }

    @Test
    void login_shouldReturnLoginView() {
        assertThat(authController.login()).isEqualTo("auth/login");
        verify(userRepository, times(0)).findByUsername(any());
    }
}
