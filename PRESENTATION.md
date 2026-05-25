---
marp: true
theme: default
paginate: true
size: 16:9
---

# Онлайн библиотека
## Spring Boot + Spring Security
### Реализация требований безопасности и flow пользователя

Автор: Григоровский Денис

---

## Что входит в проект

**Стек:** Java 17, Spring Boot 3.2, Spring MVC, Spring Security, Spring Data JPA, Thymeleaf, H2.

**Flow пользователя, который требовался по заданию:**

1. **Регистрация** нового пользователя — `/register`
2. **Авторизация** (вход) — `/login`
3. **Действия, специфичные для роли** — каталог книг для USER, управление книгами и пользователями для ADMIN
4. **Делогирование** (выход) — `/logout`

Презентация разбита на **3 блока**:

- **Блок 1.** База данных и регистрация (хэширование паролей)
- **Блок 2.** Авторизация и роли
- **Блок 3.** Role-based защита и выход

---

# Блок 1. База данных и регистрация
## Слайды 3–5

---

## Слайд 3. Подключение к базе данных

**Файл:** `src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:h2:file:./data/librarydb;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
```

**Что делает:**

- Подключается к встроенной БД **H2** в файле `./data/librarydb`.
- `ddl-auto=create-drop` — Hibernate сам создаёт таблицы по JPA-сущностям.
- Включена web-консоль H2 для отладки.

Сущности (`Book`, `User`) и репозитории (`BookRepository`, `UserRepository extends JpaRepository`) автоматически связываются с этой БД.

---

## Слайд 4. Сущность пользователя в БД

**Файл:** `src/main/java/com/example/library/model/User.java`

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;   // логин

    private String password;   // BCrypt-хэш, не открытый текст

    private String role;       // ROLE_USER или ROLE_ADMIN
}
```

**Что делает:**

- `@Entity` + `@Table("users")` — JPA создаёт таблицу `users`.
- `@Column(unique = true)` — два пользователя с одинаковым логином невозможны.
- Поле `role` хранит роль, по которой потом Spring Security разграничивает доступ.

---

## Слайд 5. Регистрация + хэширование паролей

**Файл:** `src/main/java/com/example/library/controller/AuthController.java`

```java
@PostMapping("/register")
public String register(@ModelAttribute User user, RedirectAttributes ra) {
    if (userRepository.findByUsername(user.getUsername()).isPresent()) {
        ra.addFlashAttribute("error", "Пользователь уже существует");
        return "redirect:/register";
    }
    user.setPassword(passwordEncoder.encode(user.getPassword())); // BCrypt
    user.setRole("ROLE_USER");
    userRepository.save(user);
    return "redirect:/login";
}
```

**Что делает:**

- Проверяет, что логин не занят.
- **Хэширует пароль через `BCryptPasswordEncoder`** — в БД попадает только хэш.
- Жёстко ставит роль `ROLE_USER` (через форму нельзя стать админом).
- Сохраняет в БД и отправляет на страницу входа.

---

# Блок 2. Авторизация и роли
## Слайды 6–8

---

## Слайд 6. Конфигурация безопасности — общая

**Файл:** `src/main/java/com/example/library/security/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity              // включает @PreAuthorize
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();   // алгоритм хэширования
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userDetailsService); // грузит юзера из БД
        auth.setPasswordEncoder(passwordEncoder());     // сверяет BCrypt
        return auth;
    }
}
```

**Что делает:** включает Spring Security, регистрирует BCrypt и провайдер, который умеет логинить пользователя по данным из БД.

---

## Слайд 7. Авторизация (вход)

**Файл:** `SecurityConfig.java` (внутри `filterChain`)

```java
.formLogin(form -> form
    .loginPage("/login")
    .defaultSuccessUrl("/books", true)
    .permitAll()
)
```

**Файл:** `CustomUserDetailsService.java`

```java
@Override
public UserDetails loadUserByUsername(String username) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return new org.springframework.security.core.userdetails.User(
        user.getUsername(),
        user.getPassword(),
        Collections.singleton(new SimpleGrantedAuthority(user.getRole()))
    );
}
```

**Что делает:** при логине Spring Security вызывает `loadUserByUsername`, получает хэш и роль из БД, сверяет введённый пароль с BCrypt-хэшем, кладёт пользователя в сессию.

---

## Слайд 8. Роли пользователей

**Файл:** `src/main/java/com/example/library/DataInitializer.java`

```java
if (userRepository.findByUsername("user").isEmpty()) {
    userRepository.save(new User("user", encoder.encode("password"), "ROLE_USER"));
}
if (userRepository.findByUsername("admin").isEmpty()) {
    userRepository.save(new User("admin", encoder.encode("admin"), "ROLE_ADMIN"));
}
```

В системе предусмотрены две роли:

| Роль         | Тестовый аккаунт       | Что разрешено                              |
|--------------|------------------------|--------------------------------------------|
| `ROLE_USER`  | `user` / `password`    | Просмотр каталога и карточек книг          |
| `ROLE_ADMIN` | `admin` / `admin`      | Всё то же + добавлять/редактировать/удалять книги, управлять пользователями |

**Что делает `DataInitializer`:** при старте приложения создаёт тестового пользователя и админа, чтобы было с чем демонстрировать flow.

---

# Блок 3. Role-based защита и выход
## Слайды 9–11

---

## Слайд 9. Защита эндпоинтов по ролям (URL-level)

**Файл:** `SecurityConfig.java` (внутри `filterChain`)

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/css/**", "/login", "/register", "/logout").permitAll()

    .requestMatchers("/admin/**").hasRole("ADMIN")

    .requestMatchers("/books/new",
                     "/books/*/edit",
                     "/books/*/delete").hasRole("ADMIN")
    .requestMatchers(HttpMethod.POST, "/books").hasRole("ADMIN")

    .requestMatchers("/", "/books", "/books/*").hasAnyRole("USER", "ADMIN")

    .anyRequest().authenticated()
)
.exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"));
```

**Что делает:** это и есть **role-based authentication**. Запрос идёт через цепочку фильтров; если у роли нет прав на URL — пользователь уходит на `/access-denied`.

---

## Слайд 10. Защита на уровне методов (`@PreAuthorize`)

**Файлы:** `BookController.java`, `AdminController.java`

```java
@PostMapping("/books/{id}/delete")
@PreAuthorize("hasRole('ADMIN')")
public String delete(@PathVariable Long id, RedirectAttributes ra) {
    repository.deleteById(id);
    ra.addFlashAttribute("success", "Книга удалена");
    return "redirect:/books";
}
```

```java
@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @GetMapping("/admin/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }
}
```

**Что делает:** даже если кто-то изменит правила URL, метод сам не выполнится без `ROLE_ADMIN`. **Двойная защита.** Работает благодаря `@EnableMethodSecurity` в `SecurityConfig`.

---

## Слайд 11. Делогирование (logout)

**Файл:** `SecurityConfig.java`

```java
.logout(logout -> logout
    .logoutUrl("/logout")
    .logoutSuccessUrl("/login?logout")
    .invalidateHttpSession(true)
    .deleteCookies("JSESSIONID")
    .permitAll()
)
```

**Файл:** `templates/books/list.html` (кнопка выхода)

```html
<form th:action="@{/logout}" method="post">
    <input type="hidden" th:name="${_csrf.parameterName}"
                         th:value="${_csrf.token}"/>
    <button type="submit">Выйти</button>
</form>
```

**Что делает:** при нажатии «Выйти» сессия инвалидируется, кука `JSESSIONID` удаляется, пользователь редиректится на `/login?logout`. CSRF-токен обязателен.

---

## Слайд 12. Итог: полный flow в приложении

| Этап задания                                     | URL                              | Где в коде                                                |
|--------------------------------------------------|----------------------------------|-----------------------------------------------------------|
| **Регистрация**                                  | `POST /register`                 | `AuthController.register` + BCrypt                        |
| **Авторизация**                                  | `POST /login`                    | `SecurityConfig.formLogin` + `CustomUserDetailsService`   |
| **Действия только для USER**                     | `GET /books`, `GET /books/{id}`  | `BookController.list`, `view`                             |
| **Действия только для ADMIN**                    | `/books/new`, `/books/*/edit`, `/books/*/delete`, `/admin/users` | `BookController` + `AdminController` + `@PreAuthorize`   |
| **Делогирование**                                | `POST /logout`                   | `SecurityConfig.logout`                                   |

**Все требования задания выполнены:**

- Подключение к БД (H2 + JPA)
- Регистрация / авторизация / logout
- Хэширование паролей BCrypt
- Две роли (`ROLE_USER`, `ROLE_ADMIN`)
- Role-based защита на уровне URL **и** методов
