package com.backend.subscribe.entity;

import com.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(
        name = "subscribes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_subscribe_user_creator",
                columnNames = {"user_id","creator_id"}
        )
)
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Getter
public class Subscribe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User creator;

    @CreatedDate
    @Column(nullable = false ,updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDate expiredAt;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscribeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscribeType type;


    private Subscribe(
            User subscriber,
            User creator,
            SubscribeStatus status,
            LocalDate expiredAt,
            SubscribeType type
    ) {
        this.user = subscriber;
        this.creator = creator;
        this.status = status;
        this.expiredAt = expiredAt;
        this.type = type;
    }

    public static Subscribe of(
            User subscriber,
            User creator,
            SubscribeStatus status,
            LocalDate expiredAt,
            SubscribeType type
    ) {
        return new Subscribe(
                subscriber,
                creator,
                status,
                expiredAt,
                type
        );
    }

    public void cancel() {
        this.status = SubscribeStatus.CANCELED;
    }

    public void activate() {
        this.status = SubscribeStatus.ACTIVE;
    }

    public void changeToFree() {
        this.type = SubscribeType.FREE;
    }

    public void changeToPaid() {
        this.type = SubscribeType.PAID;
    }

    public void renewFree(){
        this.expiredAt = null;
        changeToFree();
    }

    public void renewMembership(LocalDate newExpiredAt){
        this.expiredAt = newExpiredAt;
        changeToPaid();
    }
}
