package com.ecommerce.project.user.internal.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

    private static final String VALID_PASSWORD_HASH = "$2a$10$" + "a".repeat(53);
    private static final String CHANGED_PASSWORD_HASH = "$2b$12$" + "b".repeat(53);

    @Test
    @DisplayName("사용자는 역할이 고정되고 활성 상태로 생성된다")
    void user_starts_active_with_role() {
        // given & when
        User user = User.of("사용자", "User@example.com", VALID_PASSWORD_HASH, UserRole.BUYER);

        // then
        assertEquals("사용자", user.getName());
        assertEquals("User@example.com", user.getEmail());
        assertEquals(VALID_PASSWORD_HASH, user.getPasswordHash());
        assertEquals(UserRole.BUYER, user.getRole());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    @DisplayName("사용자 이름은 앞뒤 공백을 제외하고 2자 이상 6자 이하여야 한다")
    void user_name_must_have_valid_length() {
        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> User.of(null, "user@example.com", VALID_PASSWORD_HASH, UserRole.BUYER)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> User.of("   ", "user@example.com", VALID_PASSWORD_HASH, UserRole.BUYER)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> User.of("a", "user@example.com", VALID_PASSWORD_HASH, UserRole.BUYER)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> User.of("1234567", "user@example.com", VALID_PASSWORD_HASH, UserRole.BUYER)
        );

        User user = User.of(" 사용자 ", "user@example.com", VALID_PASSWORD_HASH, UserRole.BUYER);
        assertEquals("사용자", user.getName());
    }

    @Test
    @DisplayName("이메일은 local-part@domain 형식이어야 하며 입력값을 정규화하지 않는다")
    void email_must_have_basic_format_and_is_not_normalized() {
        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> User.of("사용자", null, VALID_PASSWORD_HASH, UserRole.BUYER)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> User.of("사용자", "user", VALID_PASSWORD_HASH, UserRole.BUYER)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> User.of("사용자", "@example.com", VALID_PASSWORD_HASH, UserRole.BUYER)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> User.of("사용자", "user@", VALID_PASSWORD_HASH, UserRole.BUYER)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> User.of("사용자", "user @example.com", VALID_PASSWORD_HASH, UserRole.BUYER)
        );

        User user = User.of("사용자", "User@Example.com", VALID_PASSWORD_HASH, UserRole.BUYER);
        assertEquals("User@Example.com", user.getEmail());
    }

    @Test
    @DisplayName("비밀번호 해시는 BCrypt 형식이어야 한다")
    void password_hash_must_have_bcrypt_format() {
        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> User.of("사용자", "user@example.com", null, UserRole.BUYER)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> User.of("사용자", "user@example.com", "password", UserRole.BUYER)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> User.of("사용자", "user@example.com", "$2a$10$short", UserRole.BUYER)
        );
    }

    @Test
    @DisplayName("사용자 역할은 필수이다")
    void user_role_is_required() {
        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> User.of("사용자", "user@example.com", VALID_PASSWORD_HASH, null)
        );
    }

    @Test
    @DisplayName("사용자 이름은 동일한 불변식을 지키며 변경된다")
    void user_changes_name_with_invariants() {
        // given
        User user = User.of("사용자", "user@example.com", VALID_PASSWORD_HASH, UserRole.BUYER);

        // when
        user.changeName(" 변경명 ");

        // then
        assertEquals("변경명", user.getName());
        assertThrows(IllegalArgumentException.class, () -> user.changeName("a"));
        assertEquals("변경명", user.getName());
    }

    @Test
    @DisplayName("사용자는 BCrypt 비밀번호 해시를 변경할 수 있다")
    void user_changes_password_hash() {
        // given
        User user = User.of("사용자", "user@example.com", VALID_PASSWORD_HASH, UserRole.BUYER);

        // when
        user.changePasswordHash(CHANGED_PASSWORD_HASH);

        // then
        assertEquals(CHANGED_PASSWORD_HASH, user.getPasswordHash());
        assertThrows(IllegalArgumentException.class, () -> user.changePasswordHash("password"));
        assertEquals(CHANGED_PASSWORD_HASH, user.getPasswordHash());
    }

    @Test
    @DisplayName("활성 사용자는 탈퇴 상태로 전환되고 다시 활성화할 수 없다")
    void active_user_can_withdraw_only_once() {
        // given
        User user = User.of("사용자", "user@example.com", VALID_PASSWORD_HASH, UserRole.BUYER);

        // when
        user.withdraw();

        // then
        assertEquals(UserStatus.WITHDRAWN, user.getStatus());
        assertThrows(IllegalStateException.class, user::withdraw);
        assertEquals(UserStatus.WITHDRAWN, user.getStatus());
    }
}
