package com.backend;

import com.backend.payment.entity.Payment;
import com.backend.payment.repository.PaymentRepository;
import com.backend.role.entity.Role;
import com.backend.role.entity.RoleEnum;
import com.backend.role.repository.RoleRepository;
import com.backend.settlement.entity.Settlement;
import com.backend.settlement.repository.SettlementRepository;
import com.backend.settlement_item.entity.SettlementItem;
import com.backend.settlement_item.repository.SettlementItemRepository;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Set;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner init(RoleRepository roleRepository,
                                  UserRepository userRepository,
                                  PasswordEncoder passwordEncoder,
                                  PaymentRepository paymentRepository,
                                  SettlementItemRepository settlementItemRepository,
                                  SettlementRepository settlementRepository
    ) {
        return args -> {
            // ===== 회원 초기화 데이터 =====
            Role adminRole = roleRepository.save(new Role(RoleEnum.ROLE_ADMIN));
            Role creatorRole = roleRepository.save(new Role(RoleEnum.ROLE_CREATOR));
            Role userRole = roleRepository.save(new Role(RoleEnum.ROLE_USER));

            String encode = passwordEncoder.encode("password123!");

            User user1 = new User("user1@email.com", "user1", encode, Set.of(userRole));
            User user2 = new User("user2@email.com", "user2", encode, Set.of(userRole));
            User user3 = new User("user3@email.com", "user3", encode, Set.of(userRole));

            User creator1 = new User("creator1@email.com", "creator1", encode, Set.of(creatorRole));
            User creator2 = new User("creator2@email.com", "creator2", encode, Set.of(creatorRole));
            User creator3 = new User("creator3@email.com", "creator3", encode, Set.of(creatorRole));

            userRepository.save(user1);
            userRepository.save(user2);
            userRepository.save(user3);
            userRepository.save(creator1);
            userRepository.save(creator2);
            userRepository.save(creator3);

            // ===== 결제 초기화 데이터 =====
            // 2025년 12월
            Payment payment1 = Payment.create(user1, creator1, 10000,"test_payment_key_001", LocalDate.of(2025, 12, 15));
            Payment payment2 = Payment.create(user2, creator1, 12000,"test_payment_key_002", LocalDate.of(2025, 12, 20));
            Payment payment3 = Payment.create(user3, creator1, 15000,"test_payment_key_003", LocalDate.of(2025, 12, 25));

            paymentRepository.save(payment1);
            paymentRepository.save(payment2);
            paymentRepository.save(payment3);

            // 2026년 1월
            Payment payment4 = Payment.create(user1, creator1, 10000,"test_payment_key_004" , LocalDate.of(2026, 1, 15));
            Payment payment5 = Payment.create(user2, creator1, 18000,"test_payment_key_005" , LocalDate.of(2026, 1, 18));
            Payment payment6 = Payment.create(user3, creator1, 18000,"test_payment_key_006" , LocalDate.of(2026, 1, 22));

            paymentRepository.save(payment4);
            paymentRepository.save(payment5);
            paymentRepository.save(payment6);

            // ===== 정산 원장 초기화 데이터 =====
            SettlementItem recorded1 = SettlementItem.recorded(creator1.getId(), payment1.getId(), (long) payment1.getAmount());
            SettlementItem recorded2 = SettlementItem.recorded(creator1.getId(), payment2.getId(), (long) payment2.getAmount());
            SettlementItem recorded3 = SettlementItem.recorded(creator1.getId(), payment3.getId(), (long) payment3.getAmount());

            settlementItemRepository.save(recorded1);
            settlementItemRepository.save(recorded2);
            settlementItemRepository.save(recorded3);

            SettlementItem recorded4 = SettlementItem.recorded(creator1.getId(), payment4.getId(), (long) payment4.getAmount());
            SettlementItem recorded5 = SettlementItem.recorded(creator1.getId(), payment5.getId(), (long) payment5.getAmount());
            SettlementItem recorded6 = SettlementItem.recorded(creator1.getId(), payment6.getId(), (long) payment6.getAmount());

            settlementItemRepository.save(recorded4);
            settlementItemRepository.save(recorded5);
            settlementItemRepository.save(recorded6);

            // ===== 정산 초기화 데이터 =====
            LocalDate dec2025Start = YearMonth.of(2025, 12).atDay(1);
            LocalDate dec2025End = YearMonth.of(2025, 12).atEndOfMonth();
            Settlement settlement1 = Settlement.create(
                    creator1.getId(),
                    dec2025Start,
                    dec2025End,
                    recorded1.getSettlementAmount() + (recorded2.getSettlementAmount() + recorded3.getSettlementAmount())
            );

            settlementRepository.save(settlement1);

            LocalDate jan2026Start = YearMonth.of(2026, 1).atDay(1);
            LocalDate jan2026End = YearMonth.of(2026, 1).atEndOfMonth();
            Settlement settlement2 = Settlement.create(
                    creator1.getId(),
                    jan2026Start,
                    jan2026End,
                    recorded4.getSettlementAmount() + (recorded5.getSettlementAmount() + recorded6.getSettlementAmount())
            );

            settlementRepository.save(settlement2);
        };
    }
}
