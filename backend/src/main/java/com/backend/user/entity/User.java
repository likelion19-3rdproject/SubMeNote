package com.backend.user.entity;

import com.backend.role.entity.Role;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

@Table(name = "users")
@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "nickname", unique = true)
    private String nickname;

    @Column(name = "password")
    private String password;

    @ManyToMany
    @JoinTable(
            name = "user_role",
            joinColumns = {@JoinColumn(name = "user_id", nullable = false)},
            inverseJoinColumns = {@JoinColumn(name = "role_id", nullable = false)}
    )
    private Set<Role> role;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 회원 role 확인
    public boolean hasRole(String roleName) {
        return role.stream()
                .anyMatch(role -> role.getRole().equals(roleName));
    }
}