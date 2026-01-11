package com.backend.role.entity;

import jakarta.persistence.*;

@Table(name = "roles")
@Entity
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private short id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleEnum role;
}
