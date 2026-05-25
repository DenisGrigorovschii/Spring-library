package com.example.library.repository;

import com.example.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Интерфейс UserRepository - репозиторий для работы с пользователями
 * 
 * Репозиторий - это слой доступа к данным, который упрощает работу с базой данных.
 * Spring Data JPA автоматически создаёт реализацию этого интерфейса.
 * 
 * Наследуется от JpaRepository, что даёт нам готовые методы:
 * - save(User) - сохранить пользователя
 * - findById(Long) - найти по ID
 * - findAll() - получить всех пользователей
 * - deleteById(Long) - удалить по ID
 * - count() - подсчитать количество
 * и многие другие стандартные операции с БД
 * 
 * @param <User> тип сущности (модель User)
 * @param <Long> тип первичного ключа (ID пользователя)
 */
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Найти пользователя по логину (username)
     * 
     * Spring Data JPA автоматически создаёт реализацию этого метода,
     * анализируя имя метода. Правило: findBy + имя поля (с заглавной буквы)
     * 
     * @param username логин пользователя для поиска
     * @return Optional<User> - контейнер, который может содержать пользователя или быть пустым
     * 
     * Использование Optional позволяет безопасно обрабатывать случай,
     * когда пользователь с таким логином не найден (без NullPointerException)
     * 
     * Пример использования:
     * Optional<User> user = userRepository.findByUsername("admin");
     * if (user.isPresent()) {
     *     User foundUser = user.get();
     * }
     */
    Optional<User> findByUsername(String username);
}


