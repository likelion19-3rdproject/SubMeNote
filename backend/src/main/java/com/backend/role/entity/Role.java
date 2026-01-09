package com.backend.role.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private short id;

    @Column(nullable = false)
    private RoleEnum role;

    public Role(RoleEnum role) {
        this.role = role;
    }
}
