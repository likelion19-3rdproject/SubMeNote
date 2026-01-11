package com.backend.role.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Table(name = "roles")
@Entity
@Getter
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private short id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleEnum role;
}
