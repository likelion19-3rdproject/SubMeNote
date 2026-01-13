package com.backend.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bank_name", nullable = false, length = 10)
    private String bankName;

    @Column(name = "account_number", nullable = false, length = 30)
    private String accountNumber;

    @Column(name = "holder_name", nullable = false, length = 10)
    private String holderName;

    public Account(String bankName, String accountNumber, String holderName) {
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.holderName = holderName;
    }

    // 수정
    public void update(String bankName, String accountNumber, String holderName) {
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.holderName = holderName;
    }
}
