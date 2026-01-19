package com.backend.role.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@NoArgsConstructor
@Getter
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleEnum role;

    private Role(RoleEnum role) {
        this.role = role;
    }

    public static Role of(RoleEnum role){
        return new Role(role);
    }
}
