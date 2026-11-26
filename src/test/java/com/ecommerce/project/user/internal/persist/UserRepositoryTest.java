package com.ecommerce.project.user.internal.persist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ecommerce.project.support.MySqlDataJpaTest;
import com.ecommerce.project.user.internal.domain.User;
import com.ecommerce.project.user.internal.domain.UserRole;
import com.ecommerce.project.user.internal.domain.UserStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

@MySqlDataJpaTest
class UserRepositoryTest {

    private static final String PASSWORD_HASH = "$2a$10$" + "a".repeat(53);

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 Repository로 사용자를 저장하고 조회할 수 있다")
    void user_can_be_saved_and_found() {
        // given
        User user = User.of("사용자", "user@example.com", PASSWORD_HASH, UserRole.BUYER);

        // when
        User savedUser = userRepository.saveAndFlush(user);
        Long userId = savedUser.getId();
        entityManager.clear();
        User foundUser = userRepository.findById(userId).orElseThrow();

        // then
        assertNotNull(userId);
        assertEquals("사용자", foundUser.getName());
        assertEquals("user@example.com", foundUser.getEmail());
        assertEquals(PASSWORD_HASH, foundUser.getPasswordHash());
        assertEquals(UserRole.BUYER, foundUser.getRole());
        assertEquals(UserStatus.ACTIVE, foundUser.getStatus());
    }

    @Test
    @DisplayName("같은 이메일이라도 역할이 다르면 저장할 수 있다")
    void same_email_with_different_roles_is_allowed() {
        // given
        String email = "user@example.com";
        User buyer = User.of("구매자", email, PASSWORD_HASH, UserRole.BUYER);
        User seller = User.of("판매자", email, PASSWORD_HASH, UserRole.SELLER);

        // when
        userRepository.saveAndFlush(buyer);
        userRepository.saveAndFlush(seller);

        // then
        assertTrue(userRepository.existsByEmailAndRole(email, UserRole.BUYER));
        assertTrue(userRepository.existsByEmailAndRole(email, UserRole.SELLER));
    }

    @Test
    @DisplayName("이메일 중복 여부는 입력된 문자열을 대소문자까지 그대로 비교한다")
    void email_comparison_is_case_sensitive() {
        // given
        userRepository.saveAndFlush(
                User.of("사용자", "User@Example.com", PASSWORD_HASH, UserRole.BUYER)
        );
        entityManager.clear();

        // when & then
        assertTrue(userRepository.existsByEmailAndRole("User@Example.com", UserRole.BUYER));
        assertFalse(userRepository.existsByEmailAndRole("user@example.com", UserRole.BUYER));
    }

    @Test
    @DisplayName("같은 이메일과 역할의 사용자는 중복 저장할 수 없다")
    void same_email_and_role_is_rejected() {
        // given
        String email = "user@example.com";
        userRepository.saveAndFlush(User.of("사용자", email, PASSWORD_HASH, UserRole.BUYER));

        // when & then
        assertThrows(
                DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(
                        User.of("다른이름", email, PASSWORD_HASH, UserRole.BUYER)
                )
        );
    }
}
