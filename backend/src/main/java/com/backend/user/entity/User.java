package com.backend.user.entity;

import com.backend.role.entity.Role;
import com.backend.role.entity.RoleEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "nickname", unique = true, nullable = false)
    private String nickname;

    @Column(name = "password", nullable = false)
    private String password;

    @ManyToMany
    @JoinTable(
            name = "user_role",
            joinColumns = {@JoinColumn(name = "user_id", nullable = false)},
            inverseJoinColumns = {@JoinColumn(name = "role_id", nullable = false)}
    )
    private Set<Role> role;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "account_id")
    private Account account;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate(){
        createdAt = LocalDateTime.now();
    }

    // 회원 role 확인
    public boolean hasRole(RoleEnum roleName) {
        return role.stream()
                .anyMatch(role -> role.getRole().equals(roleName));
    }

    // 계좌 등록
    public void registerAccount(Account account) {
        this.account = account;
    }

    // 계좌 등록 여부 확인
    public boolean hasAccount() {
        return account != null;
    }

    public User(String email, String nickname, String password, Set<Role> role) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }
}