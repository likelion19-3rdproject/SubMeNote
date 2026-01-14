package com.backend;

import com.backend.order.entity.Order;
import com.backend.order.entity.OrderStatus;
import com.backend.order.repository.OrderRepository;
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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

@EnableScheduling
@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner init(RoleRepository roleRepository,
                                  UserRepository userRepository,
                                  PasswordEncoder passwordEncoder,
                                  OrderRepository orderRepository,
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

            userRepository.saveAll(List.of(user1, user2, user3));
            userRepository.saveAll(List.of(creator1, creator2, creator3));

            // ===== 주문 초기화 데이터
            Order order1 = new Order(user1, creator1, "ORDER_001", "creator1-구독", 10000, "CARD", OrderStatus.PENDING);
            Order order2 = new Order(user2, creator1, "ORDER_002", "creator1-구독", 12000, "CARD", OrderStatus.PENDING);
            Order order3 = new Order(user3, creator1, "ORDER_003", "creator1-구독", 15000, "CARD", OrderStatus.PENDING);

            Order order4 = new Order(user1, creator1, "ORDER_004", "creator1-구독", 10000, "CARD", OrderStatus.PENDING);
            Order order5 = new Order(user2, creator1, "ORDER_005", "creator1-구독", 17000, "CARD", OrderStatus.PENDING);
            Order order6 = new Order(user3, creator1, "ORDER_006", "creator1-구독", 18000, "CARD", OrderStatus.PENDING);

            orderRepository.saveAll(List.of(order1, order2, order3, order4, order5, order6));

            // ===== 결제 초기화 데이터 =====
            // 2025년 12월
            Payment payment1 = Payment.create(order1, "test_payment_key_001", LocalDate.of(2025, 12, 15));
            Payment payment2 = Payment.create(order2, "test_payment_key_002", LocalDate.of(2025, 12, 20));
            Payment payment3 = Payment.create(order3, "test_payment_key_003", LocalDate.of(2025, 12, 25));

            paymentRepository.saveAll(List.of(payment1, payment2, payment3));

            // 2026년 1월
            Payment payment4 = Payment.create(order4, "test_payment_key_004", LocalDate.of(2026, 1, 15));
            Payment payment5 = Payment.create(order5, "test_payment_key_005", LocalDate.of(2026, 1, 18));
            Payment payment6 = Payment.create(order6, "test_payment_key_006", LocalDate.of(2026, 1, 22));

            paymentRepository.saveAll(List.of(payment4, payment5, payment6));

            // ===== 정산 원장 초기화 데이터 =====
            SettlementItem recorded1 = SettlementItem.create(payment1);
            SettlementItem recorded2 = SettlementItem.create(payment2);
            SettlementItem recorded3 = SettlementItem.create(payment3);

            settlementItemRepository.saveAll(List.of(recorded1, recorded2, recorded3));

            SettlementItem recorded4 = SettlementItem.create(payment4);
            SettlementItem recorded5 = SettlementItem.create(payment5);
            SettlementItem recorded6 = SettlementItem.create(payment6);

            settlementItemRepository.saveAll(List.of(recorded4, recorded5, recorded6));

            // ===== 정산 초기화 데이터 =====
            LocalDate dec2025Start = YearMonth.of(2025, 12).atDay(1);
            LocalDate dec2025End = YearMonth.of(2025, 12).atEndOfMonth();
            Settlement settlement1 = Settlement.create(
                    creator1,
                    dec2025Start,
                    dec2025End,
                    recorded1.getSettlementAmount() + recorded2.getSettlementAmount() + recorded3.getSettlementAmount()
            );

            LocalDate jan2026Start = YearMonth.of(2026, 1).atDay(1);
            LocalDate jan2026End = YearMonth.of(2026, 1).atEndOfMonth();
            Settlement settlement2 = Settlement.create(
                    creator1,
                    jan2026Start,
                    jan2026End,
                    recorded4.getSettlementAmount() + recorded5.getSettlementAmount() + recorded6.getSettlementAmount()
            );

            settlementRepository.saveAll(List.of(settlement1, settlement2));

            recorded1.assignToSettlement(settlement1);
            recorded2.assignToSettlement(settlement1);
            recorded3.assignToSettlement(settlement1);

            recorded4.assignToSettlement(settlement2);
            recorded5.assignToSettlement(settlement2);
            recorded6.assignToSettlement(settlement2);

            settlementItemRepository.saveAll(
                    List.of(recorded1, recorded2, recorded3, recorded4, recorded5, recorded6)
            );
        };
    }
}
