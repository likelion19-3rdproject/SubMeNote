package com.backend.user.service.impl;

import com.backend.global.exception.common.BusinessException;
import com.backend.global.exception.domain.UserErrorCode;
import com.backend.global.validator.RoleValidator;
import com.backend.user.dto.AccountRequestDto;
import com.backend.user.dto.AccountResponseDto;
import com.backend.user.entity.Account;
import com.backend.user.entity.User;
import com.backend.user.repository.AccountRepository;
import com.backend.user.repository.UserRepository;
import com.backend.user.service.CreatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatorServiceImpl implements CreatorService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final RoleValidator roleValidator;

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

        User creator = roleValidator.validateCreator(userId);

        if (creator.hasAccount()) {
            throw new BusinessException(UserErrorCode.ACCOUNT_ALREADY_REGISTERED);
        }

        Account account = Account.of(
                requestDto.bankName(),
                requestDto.accountNumber(),
                requestDto.holderName());

        creator.registerAccount(account);

        accountRepository.save(account);
    }

    /**
     * 크리에이터 계좌 조회
     */
    @Override
    @Transactional(readOnly = true)
    public AccountResponseDto getAccount(Long userId) {

        User creator = roleValidator.validateCreator(userId);

        if (creator.getAccount() == null) {
            throw new BusinessException(UserErrorCode.ACCOUNT_NOT_FOUND);
        }

        return AccountResponseDto.from(creator.getAccount());
    }

    /**
     * 크리에이터 계좌 수정
     */
    @Override
    @Transactional
    public void updateAccount(Long userId, AccountRequestDto requestDto) {

        User creator = roleValidator.validateCreator(userId);

        Account account = creator.getAccount();
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
