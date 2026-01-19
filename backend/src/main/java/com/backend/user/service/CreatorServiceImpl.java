package com.backend.user.service;

import com.backend.global.exception.common.BusinessException;
import com.backend.global.exception.domain.UserErrorCode;
import com.backend.role.entity.RoleEnum;
import com.backend.user.dto.AccountRequestDto;
import com.backend.user.dto.AccountResponseDto;
import com.backend.user.entity.Account;
import com.backend.user.entity.User;
import com.backend.user.repository.AccountRepository;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatorServiceImpl implements CreatorService{

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    /**
     * 크리에이터 계좌 등록
     * <br/>
     * 1. 존재하는 유저인지 확인
     * 2. 크리에이터인지 확인
     * 3. 이미 등록된 계좌가 있는지 확인
     */
    @Override
    @Transactional
    public void registerAccount(Long userId, AccountRequestDto requestDto) {

        User user = userRepository.findByIdOrThrow(userId);

        if (!user.hasRole(RoleEnum.ROLE_CREATOR)) {
            throw new BusinessException(UserErrorCode.CREATOR_FORBIDDEN);
        }

        if (user.hasAccount()) {
            throw new BusinessException(UserErrorCode.ACCOUNT_ALREADY_REGISTERED);
        }

        Account account = new Account(
                requestDto.bankName(),
                requestDto.accountNumber(),
                requestDto.holderName());

        user.registerAccount(account);

        accountRepository.save(account);
    }

    /**
     * 크리에이터 계좌 조회
     */
    @Override
    @Transactional(readOnly = true)
    public AccountResponseDto getAccount(Long userId) {

        User user = userRepository.findByIdOrThrow(userId);

        if (!user.hasRole(RoleEnum.ROLE_CREATOR)) {
            throw new BusinessException(UserErrorCode.CREATOR_FORBIDDEN);
        }

        if (user.getAccount() == null) {
            throw new BusinessException(UserErrorCode.ACCOUNT_NOT_FOUND);
        }

        return AccountResponseDto.from(user.getAccount());
    }

    /**
     * 크리에이터 계좌 수정
     */
    @Override
    @Transactional
    public void updateAccount(Long userId, AccountRequestDto requestDto) {

        User user = userRepository.findByIdOrThrow(userId);

        if (!user.hasRole(RoleEnum.ROLE_CREATOR)) {
            throw new BusinessException(UserErrorCode.CREATOR_FORBIDDEN);
        }

        Account account = user.getAccount();
        if (account == null) {
            throw new BusinessException(UserErrorCode.ACCOUNT_NOT_FOUND);
        }

        account.update(
                requestDto.bankName(),
                requestDto.accountNumber(),
                requestDto.holderName()
        );
    }
}
