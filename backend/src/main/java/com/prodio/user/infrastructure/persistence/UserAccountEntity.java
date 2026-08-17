package com.prodio.user.infrastructure.persistence;

import java.util.LinkedHashSet;
import java.util.Set;
import com.prodio.user.domain.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "user_accounts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class UserAccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserAccount.Status status;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @OrderBy("code ASC")
    private Set<UserRoleEntity> roles = new LinkedHashSet<>();

    UserAccountEntity(String email, String passwordHash, String name) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.status = UserAccount.Status.ACTIVE;
    }

    void addRole(UserRoleEntity role) {
        this.roles.add(role);
    }

    void replaceRoles(Set<UserRoleEntity> roles) {
        this.roles.clear();
        this.roles.addAll(roles);
    }

    UserAccount toDomain() {
        return new UserAccount(id, email, name, status);
    }
}
