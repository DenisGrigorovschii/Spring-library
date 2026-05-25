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
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты для AdminController: список пользователей,
 * переключение роли и удаление пользователя.
 */
@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AdminController adminController;

    private User regularUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        regularUser = new User("john", "HASH", "ROLE_USER");
        regularUser.setId(2L);

        adminUser = new User("admin", "HASH", "ROLE_ADMIN");
        adminUser.setId(1L);
    }

    @Test
    void users_shouldPutUsersIntoModel() {
        when(userRepository.findAll()).thenReturn(List.of(regularUser, adminUser));

        Model model = new ExtendedModelMap();
        String view = adminController.users(model);

        assertThat(view).isEqualTo("admin/users");
        assertThat(model.getAttribute("users"))
                .isEqualTo(List.of(regularUser, adminUser));
    }

    @Test
    void toggleRole_shouldPromoteUserToAdmin() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));
        when(authentication.getName()).thenReturn("admin");

        String view = adminController.toggleRole(2L, authentication, redirectAttributes);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo("ROLE_ADMIN");
        assertThat(view).isEqualTo("redirect:/admin/users");
    }

    @Test
    void toggleRole_shouldDemoteAdminToUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(authentication.getName()).thenReturn("someoneElse");

        adminController.toggleRole(1L, authentication, redirectAttributes);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo("ROLE_USER");
    }

    @Test
    void toggleRole_shouldRefuse_whenChangingSelf() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(authentication.getName()).thenReturn("admin");           // тот же логин

        String view = adminController.toggleRole(1L, authentication, redirectAttributes);

        verify(userRepository, never()).save(any());
        verify(redirectAttributes).addFlashAttribute(eq("error"), any());
        assertThat(view).isEqualTo("redirect:/admin/users");
    }

    @Test
    void toggleRole_shouldFail_whenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        String view = adminController.toggleRole(99L, authentication, redirectAttributes);

        verify(userRepository, never()).save(any());
        verify(redirectAttributes).addFlashAttribute(eq("error"), any());
        assertThat(view).isEqualTo("redirect:/admin/users");
    }

    @Test
    void deleteUser_shouldRemoveOtherUser() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));
        when(authentication.getName()).thenReturn("admin");

        String view = adminController.deleteUser(2L, authentication, redirectAttributes);

        verify(userRepository).deleteById(2L);
        verify(redirectAttributes).addFlashAttribute(eq("success"), any());
        assertThat(view).isEqualTo("redirect:/admin/users");
    }

    @Test
    void deleteUser_shouldRefuse_whenDeletingSelf() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(authentication.getName()).thenReturn("admin");           // удаляет сам себя

        adminController.deleteUser(1L, authentication, redirectAttributes);

        verify(userRepository, never()).deleteById(any());
        verify(redirectAttributes).addFlashAttribute(eq("error"), any());
    }
}
