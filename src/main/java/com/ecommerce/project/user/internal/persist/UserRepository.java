package com.ecommerce.project.user.internal.persist;

import com.ecommerce.project.user.internal.domain.User;
import com.ecommerce.project.user.internal.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailAndRole(String email, UserRole role);
}
