package com.ecommerce.project.payment.internal.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PaymentTest {

    private static final String PG_ORDER_ID = "pg-order-123456";
    private static final String IDEMPOTENCY_KEY = "approval-idempotency-key-123";
    private static final String PAYMENT_KEY = "payment-key-123";
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 11, 26, 10, 0);
    private static final LocalDateTime EXPIRES_AT = CREATED_AT.plusMinutes(15);

    @Test
    @DisplayName("결제는 변경할 수 없는 요청 정보와 준비 상태로 생성된다")
    void payment_starts_ready() {
        // given & when
        Payment payment = createPayment();

        // then
        assertEquals(1L, payment.getOrderId());
        assertEquals(10_000L, payment.getAmount());
        assertEquals(PG_ORDER_ID, payment.getPgOrderId());
        assertEquals(IDEMPOTENCY_KEY, payment.getPgApprovalIdempotencyKey());
        assertEquals(CREATED_AT, payment.getCreatedAt());
        assertEquals(EXPIRES_AT, payment.getExpiresAt());
        assertEquals(PaymentStatus.READY, payment.getStatus());
        assertNull(payment.getPgPaymentKey());
    }

    @Test
    @DisplayName("결제 대상 주문과 PG 요청 식별자는 필수이다")
    void payment_requires_order_and_pg_identifiers() {
        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> Payment.of(null, 10_000L, PG_ORDER_ID, IDEMPOTENCY_KEY, CREATED_AT, EXPIRES_AT)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> Payment.of(1L, 10_000L, " ", IDEMPOTENCY_KEY, CREATED_AT, EXPIRES_AT)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> Payment.of(1L, 10_000L, PG_ORDER_ID, " ", CREATED_AT, EXPIRES_AT)
        );
    }

    @Test
    @DisplayName("결제 요청 금액은 0 이상이어야 한다")
    void payment_amount_must_be_non_negative() {
        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> Payment.of(1L, -1L, PG_ORDER_ID, IDEMPOTENCY_KEY, CREATED_AT, EXPIRES_AT)
        );
    }

    @Test
    @DisplayName("결제 만료 시각은 생성 시각보다 이후여야 한다")
    void payment_expiration_must_be_after_creation() {
        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> Payment.of(1L, 10_000L, PG_ORDER_ID, IDEMPOTENCY_KEY, null, EXPIRES_AT)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> Payment.of(1L, 10_000L, PG_ORDER_ID, IDEMPOTENCY_KEY, CREATED_AT, null)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> Payment.of(1L, 10_000L, PG_ORDER_ID, IDEMPOTENCY_KEY, CREATED_AT, CREATED_AT)
        );
    }

    @Test
    @DisplayName("준비 상태의 결제는 PG 결제 키를 저장하고 승인 진행 상태가 된다")
    void ready_payment_starts_approval() {
        // given
        Payment payment = createPayment();

        // when
        payment.startApproval(PAYMENT_KEY);

        // then
        assertEquals(PAYMENT_KEY, payment.getPgPaymentKey());
        assertEquals(PaymentStatus.IN_PROGRESS, payment.getStatus());
        assertThrows(IllegalStateException.class, () -> payment.startApproval("another-payment-key"));
        assertEquals(PAYMENT_KEY, payment.getPgPaymentKey());
    }

    @Test
    @DisplayName("PG 결제 키가 없으면 승인을 시작할 수 없다")
    void approval_requires_pg_payment_key() {
        // given
        Payment payment = createPayment();

        // when & then
        assertThrows(IllegalArgumentException.class, () -> payment.startApproval(" "));
        assertEquals(PaymentStatus.READY, payment.getStatus());
        assertNull(payment.getPgPaymentKey());
    }

    @Test
    @DisplayName("승인 진행 상태는 완료, 최종 실패 또는 결과 불확실 상태로 전이할 수 있다")
    void in_progress_payment_accepts_pg_results() {
        // given
        Payment completed = createPayment();
        Payment aborted = createPayment();
        Payment unknown = createPayment();
        completed.startApproval(PAYMENT_KEY + "-done");
        aborted.startApproval(PAYMENT_KEY + "-aborted");
        unknown.startApproval(PAYMENT_KEY + "-unknown");

        // when
        completed.complete();
        aborted.abort();
        unknown.markUnknown();

        // then
        assertEquals(PaymentStatus.DONE, completed.getStatus());
        assertEquals(PaymentStatus.ABORTED, aborted.getStatus());
        assertEquals(PaymentStatus.UNKNOWN, unknown.getStatus());
        assertThrows(IllegalStateException.class, completed::abort);
        assertThrows(IllegalStateException.class, aborted::complete);
    }

    @Test
    @DisplayName("결과 불확실 상태는 PG 확인 결과에 따라 최종 상태로 전이할 수 있다")
    void unknown_payment_accepts_confirmed_pg_results() {
        // given
        Payment completed = createPayment();
        Payment aborted = createPayment();
        Payment expired = createPayment();
        completed.startApproval(PAYMENT_KEY + "-done");
        aborted.startApproval(PAYMENT_KEY + "-aborted");
        expired.startApproval(PAYMENT_KEY + "-expired");
        completed.markUnknown();
        aborted.markUnknown();
        expired.markUnknown();

        // when
        completed.complete();
        aborted.abort();
        expired.expireByPgConfirmation();

        // then
        assertEquals(PaymentStatus.DONE, completed.getStatus());
        assertEquals(PaymentStatus.ABORTED, aborted.getStatus());
        assertEquals(PaymentStatus.EXPIRED, expired.getStatus());
    }

    @Test
    @DisplayName("준비 상태의 결제는 결제 기한이 지난 뒤에만 만료할 수 있다")
    void ready_payment_expires_after_deadline() {
        // given
        Payment payment = createPayment();

        // when & then
        assertThrows(IllegalArgumentException.class, () -> payment.expire(null));
        assertThrows(IllegalStateException.class, () -> payment.expire(EXPIRES_AT.minusNanos(1)));
        assertEquals(PaymentStatus.READY, payment.getStatus());

        payment.expire(EXPIRES_AT);
        assertEquals(PaymentStatus.EXPIRED, payment.getStatus());
    }

    @Test
    @DisplayName("PG 만료 확인은 승인 진행 또는 결과 불확실 결제에만 반영할 수 있다")
    void pg_expiration_requires_pending_pg_result() {
        // given
        Payment ready = createPayment();
        Payment inProgress = createPayment();
        inProgress.startApproval(PAYMENT_KEY);

        // when & then
        assertThrows(IllegalStateException.class, ready::expireByPgConfirmation);

        inProgress.expireByPgConfirmation();
        assertEquals(PaymentStatus.EXPIRED, inProgress.getStatus());
        assertThrows(IllegalStateException.class, inProgress::complete);
    }

    private Payment createPayment() {
        return Payment.of(
                1L,
                10_000L,
                PG_ORDER_ID,
                IDEMPOTENCY_KEY,
                CREATED_AT,
                EXPIRES_AT
        );
    }
}
