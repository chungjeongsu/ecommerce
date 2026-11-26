package com.ecommerce.project.payment.internal.persist;

import com.ecommerce.project.payment.internal.domain.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPgPaymentKey(String pgPaymentKey);
}
