package ru.kata.spring.boot_security.demo.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"password", "roles"})
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
    @Min(value = 0, message = "age cannot be negative or equal to 0")
    @Max(value = 130, message = "the age limit is 120 years")
    private Integer age;


    @Column(name = "email", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @Column(name = "password", nullable = false, length = 60)
    @Pattern(regexp = "^$|.{3,60}", message = "Password must be between 3 and 60 characters")
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    @JoinTable(
    name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new LinkedHashSet<>();

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
            return Objects.equals(email, user.email);
        }
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return Objects.hash(getId());
        } else {
            return Objects.hash(email);
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
