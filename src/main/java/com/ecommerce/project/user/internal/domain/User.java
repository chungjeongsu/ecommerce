package com.ecommerce.project.user.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_users_email_role",
                columnNames = {"email", "user_role"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 6;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+$");
    private static final Pattern BCRYPT_PATTERN = Pattern.compile(
            "^\\$2[aby]\\$(?:0[4-9]|[12][0-9]|3[01])\\$[./A-Za-z0-9]{53}$"
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false, length = 20)
    private UserStatus status;

    private User(
            final String name,
            final String email,
            final String passwordHash,
            final UserRole role
    ) {
        this.name = validateName(name);
        this.email = validateEmail(email);
        this.passwordHash = validatePasswordHash(passwordHash);
        this.role = validateRole(role);
        this.status = UserStatus.ACTIVE;
    }

    public void changeName(final String name) {
        this.name = validateName(name);
    }

    public void changePasswordHash(final String passwordHash) {
        this.passwordHash = validatePasswordHash(passwordHash);
    }

    public void withdraw() {
        ensureActive();
        status = UserStatus.WITHDRAWN;
    }

    public static User of(
            final String name,
            final String email,
            final String passwordHash,
            final UserRole role
    ) {
        return new User(name, email, passwordHash, role);
    }

    // 불변식 =====================================

    private String validateName(final String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("사용자 이름은 필수입니다.");
        }

        String trimmedName = name.trim();
        if (trimmedName.length() < MIN_NAME_LENGTH || trimmedName.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("사용자 이름은 2자 이상 6자 이하여야 합니다.");
        }
        return trimmedName;
    }

    private String validateEmail(final String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("이메일은 local-part@domain 형식이어야 합니다.");
        }
        return email;
    }

    private String validatePasswordHash(final String passwordHash) {
        if (passwordHash == null || !BCRYPT_PATTERN.matcher(passwordHash).matches()) {
            throw new IllegalArgumentException("비밀번호는 BCrypt 해시 형식이어야 합니다.");
        }
        return passwordHash;
    }

    private UserRole validateRole(final UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("사용자 역할은 필수입니다.");
        }
        return role;
    }

    private void ensureActive() {
        if (status != UserStatus.ACTIVE) {
            throw new IllegalStateException("활성 상태의 사용자만 탈퇴할 수 있습니다.");
        }
    }
}
