package com.backend.profile_image.entity;

import com.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "profile_images",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_profile_image_user",
                columnNames = "user_id"
        )
)
@Getter
@NoArgsConstructor
public class ProfileImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "size", nullable = false)
    private long size;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "data", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] data;

    public ProfileImage(User user, String originalName, String contentType, long size, byte[] data) {
        this.user = user;
        this.originalName = originalName;
        this.contentType = contentType;
        this.size = size;
        this.data = data;
    }
}