package com.backend.user.dto;

import com.backend.user.entity.Account;

public record AccountResponseDto(
        String bankName,
        String accountNumber,
        String holderName
) {
    public static AccountResponseDto from(Account account) {
        return new AccountResponseDto(
                account.getBankName(),
                account.getAccountNumber(),
                account.getHolderName()
        );
    }
}
