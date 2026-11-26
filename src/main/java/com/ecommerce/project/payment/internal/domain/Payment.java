package com.ecommerce.project.payment.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "payment",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_pg_order_id", columnNames = "pg_order_id"),
                @UniqueConstraint(
                        name = "uk_payment_pg_approval_idempotency_key",
                        columnNames = "pg_approval_idempotency_key"
                ),
                @UniqueConstraint(name = "uk_payment_pg_payment_key", columnNames = "pg_payment_key")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    private static final int MAX_PG_ORDER_ID_LENGTH = 64;
    private static final int MAX_PG_PAYMENT_KEY_LENGTH = 200;
    private static final int MAX_IDEMPOTENCY_KEY_LENGTH = 300;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "pg_order_id", nullable = false, length = MAX_PG_ORDER_ID_LENGTH)
    private String pgOrderId;

    @Column(name = "pg_payment_key", length = MAX_PG_PAYMENT_KEY_LENGTH)
    private String pgPaymentKey;

    @Column(
            name = "pg_approval_idempotency_key",
            nullable = false,
            length = MAX_IDEMPOTENCY_KEY_LENGTH
    )
    private String pgApprovalIdempotencyKey;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    private Payment(
            final Long orderId,
            final long amount,
            final String pgOrderId,
            final String pgApprovalIdempotencyKey,
            final LocalDateTime createdAt,
            final LocalDateTime expiresAt
    ) {
        this.orderId = validateOrderId(orderId);
        this.amount = validateAmount(amount);
        this.pgOrderId = validateRequiredText(
                pgOrderId,
                MAX_PG_ORDER_ID_LENGTH,
                "PG 주문번호"
        );
        this.pgApprovalIdempotencyKey = validateRequiredText(
                pgApprovalIdempotencyKey,
                MAX_IDEMPOTENCY_KEY_LENGTH,
                "PG 승인 멱등키"
        );
        this.createdAt = validateCreatedAt(createdAt);
        this.expiresAt = validateExpiresAt(this.createdAt, expiresAt);
        this.status = PaymentStatus.READY;
    }

    public void startApproval(final String pgPaymentKey) {
        ensureReady();
        this.pgPaymentKey = validateRequiredText(
                pgPaymentKey,
                MAX_PG_PAYMENT_KEY_LENGTH,
                "PG 결제 키"
        );
        status = PaymentStatus.IN_PROGRESS;
    }

    public void complete() {
        ensureAwaitingPgResult();
        status = PaymentStatus.DONE;
    }

    public void abort() {
        ensureAwaitingPgResult();
        status = PaymentStatus.ABORTED;
    }

    public void markUnknown() {
        ensureInProgress();
        status = PaymentStatus.UNKNOWN;
    }

    public void expire(final LocalDateTime currentTime) {
        ensureReady();
        ensureExpirationReached(currentTime);
        status = PaymentStatus.EXPIRED;
    }

    public void expireByPgConfirmation() {
        ensureAwaitingPgResult();
        status = PaymentStatus.EXPIRED;
    }

    public static Payment of(
            final Long orderId,
            final long amount,
            final String pgOrderId,
            final String pgApprovalIdempotencyKey,
            final LocalDateTime createdAt,
            final LocalDateTime expiresAt
    ) {
        return new Payment(
                orderId,
                amount,
                pgOrderId,
                pgApprovalIdempotencyKey,
                createdAt,
                expiresAt
        );
    }

    // 불변식 =====================================

    private Long validateOrderId(final Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("주문 ID는 필수입니다.");
        }
        return orderId;
    }

    private long validateAmount(final long amount) {
        if (amount < 0L) {
            throw new IllegalArgumentException("결제 요청 금액은 0 이상이어야 합니다.");
        }
        return amount;
    }

    private String validateRequiredText(
            final String value,
            final int maxLength,
            final String fieldName
    ) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + "는 필수이며 " + maxLength + "자 이하여야 합니다.");
        }
        return value;
    }

    private LocalDateTime validateCreatedAt(final LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("결제 생성 시각은 필수입니다.");
        }
        return createdAt;
    }

    private LocalDateTime validateExpiresAt(
            final LocalDateTime createdAt,
            final LocalDateTime expiresAt
    ) {
        if (expiresAt == null || !expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("결제 만료 시각은 생성 시각보다 이후여야 합니다.");
        }
        return expiresAt;
    }

    private void ensureReady() {
        if (status != PaymentStatus.READY) {
            throw new IllegalStateException("준비 상태의 결제만 승인 또는 만료할 수 있습니다.");
        }
    }

    private void ensureInProgress() {
        if (status != PaymentStatus.IN_PROGRESS) {
            throw new IllegalStateException("승인 진행 상태의 결제만 결과 불확실 상태로 변경할 수 있습니다.");
        }
    }

    private void ensureAwaitingPgResult() {
        if (status != PaymentStatus.IN_PROGRESS && status != PaymentStatus.UNKNOWN) {
            throw new IllegalStateException("PG 결과를 기다리는 결제만 최종 결과를 반영할 수 있습니다.");
        }
    }

    private void ensureExpirationReached(final LocalDateTime currentTime) {
        if (currentTime == null) {
            throw new IllegalArgumentException("현재 시각은 필수입니다.");
        }
        if (currentTime.isBefore(expiresAt)) {
            throw new IllegalStateException("결제 기한 전에는 결제를 만료할 수 없습니다.");
        }
    }
}
