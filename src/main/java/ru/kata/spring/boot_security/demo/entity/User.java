package ru.kata.spring.boot_security.demo.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"createdAt", "updatedAt", "password", "roles"})
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 30)
    @NotBlank(message = "First name is required")
    @Size(min = 3, max = 30, message = "First name must be between 3 and 30 characters")
    private String firstName;

    @Column(name = "last_name", length = 30)
    @Size(min = 3, max = 30, message = "Last name must be between 3 and 30 characters")
    private String lastName;

    @Column(name = "age")
    @Min(value = 0, message = "возраст не может быть отрицательным или равным 0")
    @Max(value = 130, message = "возраст не может превышать 120 лет")
    private Integer age;

    @Column(name = "city", length = 30)
    private String city;

    @Column(name = "email", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @Column(name = "phone")
    @Size( min = 10, max = 12, message = "номер телефона должен быть в диапозоне 10-12 цифр")
    private String phone;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "password", nullable = false, length = 60)
    @NotBlank(message = "Password is required")
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    @JoinTable(
    name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new LinkedHashSet<>();

    public LocalDateTime getCreatedAt() {
        return createdAt.truncatedTo(ChronoUnit.SECONDS);
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt != null ? updatedAt.truncatedTo(ChronoUnit.SECONDS) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> thisClazz = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        Class<?> thatClazz = o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        if (thisClazz != thatClazz) return false;
        User user = (User) o;
        if (getId() != null & user.getId() != null) {
            return Objects.equals(getId(), user.getId());
        } else {
            return Objects.equals(email, user.email) && Objects.equals(phone, user.phone);
        }
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return Objects.hash(getId());
        } else {
            return Objects.hash(email, phone);
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
