package com.ecommerce.project.payment.internal.persist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ecommerce.project.payment.internal.domain.Payment;
import com.ecommerce.project.payment.internal.domain.PaymentStatus;
import com.ecommerce.project.support.MySqlDataJpaTest;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

@MySqlDataJpaTest
class PaymentRepositoryTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 11, 26, 10, 0);
    private static final LocalDateTime EXPIRES_AT = CREATED_AT.plusMinutes(15);

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("Payment Repository로 승인 진행 중인 결제를 저장하고 조회할 수 있다")
    void payment_can_be_saved_and_found() {
        // given
        Payment payment = createPayment(1L, "pg-order-1", "idempotency-key-1");
        payment.startApproval("PaymentKey-1");

        // when
        Payment savedPayment = paymentRepository.saveAndFlush(payment);
        Long paymentId = savedPayment.getId();
        entityManager.clear();
        Payment foundPayment = paymentRepository.findById(paymentId).orElseThrow();

        // then
        assertNotNull(paymentId);
        assertEquals(1L, foundPayment.getOrderId());
        assertEquals(10_000L, foundPayment.getAmount());
        assertEquals(PaymentStatus.IN_PROGRESS, foundPayment.getStatus());
        assertEquals("pg-order-1", foundPayment.getPgOrderId());
        assertEquals("PaymentKey-1", foundPayment.getPgPaymentKey());
        assertEquals("idempotency-key-1", foundPayment.getPgApprovalIdempotencyKey());
        assertEquals(CREATED_AT, foundPayment.getCreatedAt());
        assertEquals(EXPIRES_AT, foundPayment.getExpiresAt());
    }

    @Test
    @DisplayName("하나의 주문에 여러 결제를 저장할 수 있다")
    void multiple_payments_for_same_order_are_allowed() {
        // given
        Payment first = createPayment(1L, "pg-order-1", "idempotency-key-1");
        Payment second = createPayment(1L, "pg-order-2", "idempotency-key-2");

        // when
        paymentRepository.saveAndFlush(first);
        paymentRepository.saveAndFlush(second);

        // then
        assertEquals(2L, paymentRepository.count());
    }

    @Test
    @DisplayName("PG 주문번호는 결제 간에 중복될 수 없다")
    void pg_order_id_must_be_unique() {
        // given
        paymentRepository.saveAndFlush(
                createPayment(1L, "duplicated-pg-order", "idempotency-key-1")
        );

        // when & then
        assertThrows(
                DataIntegrityViolationException.class,
                () -> paymentRepository.saveAndFlush(
                        createPayment(2L, "duplicated-pg-order", "idempotency-key-2")
                )
        );
    }

    @Test
    @DisplayName("PG 승인 멱등키는 결제 간에 중복될 수 없다")
    void approval_idempotency_key_must_be_unique() {
        // given
        paymentRepository.saveAndFlush(
                createPayment(1L, "pg-order-1", "duplicated-idempotency-key")
        );

        // when & then
        assertThrows(
                DataIntegrityViolationException.class,
                () -> paymentRepository.saveAndFlush(
                        createPayment(2L, "pg-order-2", "duplicated-idempotency-key")
                )
        );
    }

    @Test
    @DisplayName("PG 결제 키는 대소문자를 구분해 그대로 조회한다")
    void pg_payment_key_is_case_sensitive() {
        // given
        Payment upperCaseKeyPayment = createPayment(1L, "pg-order-1", "idempotency-key-1");
        Payment lowerCaseKeyPayment = createPayment(2L, "pg-order-2", "idempotency-key-2");
        upperCaseKeyPayment.startApproval("PaymentKey");
        lowerCaseKeyPayment.startApproval("paymentkey");
        paymentRepository.saveAndFlush(upperCaseKeyPayment);
        paymentRepository.saveAndFlush(lowerCaseKeyPayment);
        entityManager.clear();

        // when & then
        assertEquals(
                upperCaseKeyPayment.getId(),
                paymentRepository.findByPgPaymentKey("PaymentKey").orElseThrow().getId()
        );
        assertEquals(
                lowerCaseKeyPayment.getId(),
                paymentRepository.findByPgPaymentKey("paymentkey").orElseThrow().getId()
        );
    }

    @Test
    @DisplayName("동일한 PG 결제 키는 여러 결제에 저장할 수 없다")
    void pg_payment_key_must_be_unique() {
        // given
        Payment first = createPayment(1L, "pg-order-1", "idempotency-key-1");
        Payment second = createPayment(2L, "pg-order-2", "idempotency-key-2");
        first.startApproval("duplicated-payment-key");
        second.startApproval("duplicated-payment-key");
        paymentRepository.saveAndFlush(first);

        // when & then
        assertThrows(
                DataIntegrityViolationException.class,
                () -> paymentRepository.saveAndFlush(second)
        );
    }

    private Payment createPayment(
            final Long orderId,
            final String pgOrderId,
            final String idempotencyKey
    ) {
        return Payment.of(
                orderId,
                10_000L,
                pgOrderId,
                idempotencyKey,
                CREATED_AT,
                EXPIRES_AT
        );
    }
}
