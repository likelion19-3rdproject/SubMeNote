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

    public Role(RoleEnum role) {
        this.role = role;
    }
}
